"""Owner-only API presentation update. No .env changes, migrations added, or test data written."""
import base64, hashlib, hmac, json, os, pathlib, shutil, subprocess, tarfile, time, urllib.request, urllib.error
os.umask(0o077)
stage=pathlib.Path(__file__).resolve().parent.parent
root=pathlib.Path('/opt/mobile-backend')
assert os.geteuid()==0 and str(stage).startswith('/opt/mobile-backend-update-') and root.is_dir()
stamp=time.strftime('%Y%m%dT%H%M%SZ',time.gmtime())
backup=root/'backups'/('api-messages-'+stamp)
backup.mkdir(mode=0o700,parents=True)
def run(args,**kwargs): return subprocess.run(args,check=True,cwd=root,**kwargs)
def compose(*args): return run(['docker','compose','--env-file','.env','-f','docker-compose.prod.yml',*args])
def query(sql):
    command='MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 --batch --skip-column-names -uroot "$MYSQL_DATABASE"'
    return subprocess.check_output(['docker','exec','-i','mobile-backend-mysql-1','sh','-c',command],input=sql.encode()).decode('utf-8').strip()
environment=dict(x.split('=',1) for x in json.loads(subprocess.check_output(['docker','inspect','mobile-backend-api-1']))[0]['Config']['Env'] if '=' in x)
def enc(value):return base64.urlsafe_b64encode(value).rstrip(b'=')
def token(uid):
    now=int(time.time())
    content=enc(b'{"alg":"HS256","typ":"JWT"}')+b'.'+enc(json.dumps({'sub':str(uid),'iat':now,'exp':now+120},separators=(',',':')).encode())
    return (content+b'.'+enc(hmac.new(environment['JWT_SECRET'].encode(),content,hashlib.sha256).digest())).decode()
def get(path,jwt=None):
    request=urllib.request.Request('http://127.0.0.1:8080'+path)
    if jwt:request.add_header('Authorization','Bearer '+jwt)
    try:
        with urllib.request.urlopen(request,timeout=10) as response:return response.status,json.load(response)
    except urllib.error.HTTPError as error:return error.code,json.load(error)
def ready():
    for _ in range(75):
        try:
            if get('/api/v1/liveness/config')[0]==401:return
        except (OSError,TimeoutError): pass
        time.sleep(2)
    raise RuntimeError('API readiness timeout')
manifest=json.loads((stage/'manifest.json').read_text())
for relative,expected in manifest.items():
    path=stage/relative
    assert not pathlib.PurePosixPath(relative).is_absolute() and '..' not in pathlib.PurePosixPath(relative).parts
    assert path.is_file() and not path.is_symlink() and hashlib.sha256(path.read_bytes()).hexdigest()==expected
print('checksums=OK',flush=True)
env_hash=hashlib.sha256((root/'.env').read_bytes()).hexdigest()
files=['src','pom.xml','Dockerfile.runtime','Dockerfile.runtime.dockerignore']
with tarfile.open(backup/'source-before.tar.gz','w:gz') as archive:
    for name in files:
        if (root/name).exists():archive.add(root/name,arcname=name)
dump='MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump --default-character-set=utf8mb4 -uroot --single-transaction --routines --triggers --events --set-gtid-purged=OFF "$MYSQL_DATABASE"'
with (backup/'database-before.sql').open('xb') as output:run(['docker','exec','mobile-backend-mysql-1','sh','-c',dump],stdout=output)
assert (backup/'database-before.sql').stat().st_size>1000
print('respaldo='+str(backup),flush=True)
before_counts=query('SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM censuses;')
roles=[row.split() for row in query('SELECT MIN(id),role FROM users GROUP BY role;').splitlines()]
before={}
for uid,role in roles:
    jwt=token(uid)
    for path in ['/api/v1/profile','/api/v1/censuses','/api/v1/censuses?origin=MASIVO','/api/v1/liveness/config']:
        code,body=get(path,jwt)
        before[(role,path)]=(code,body.get('body'))
old_image=subprocess.check_output(['docker','inspect','--format','{{.Image}}','mobile-backend-api-1'],text=True).strip()
rollback='mobile-backend-api:before-messages-'+stamp
run(['docker','tag',old_image,rollback])
(backup/'rollback-image.txt').write_text(rollback+'\n')
image='mobile-backend-api:messages-'+stamp
with (backup/'build.log').open('w') as output:
    run(['docker','build','-f',str(stage/'Dockerfile.runtime'),'-t',image,str(stage)],stdout=output,stderr=subprocess.STDOUT)
try:
    for name in files+['target']:
        path=stage/name
        if path.is_dir():shutil.copytree(path,root/name,dirs_exist_ok=True)
        elif path.is_file():shutil.copy2(path,root/name)
    run(['docker','tag',image,'mobile-backend-api:latest'])
    compose('up','-d','--no-deps','--no-build','api')
    ready()
    code,body=get('/api/v1/liveness/config')
    assert code==401 and body['httpCode']==401 and body.get('timestamp') and body['message']=='Token requerido o inválido'
    for uid,role in roles:
        jwt=token(uid)
        for path in ['/api/v1/profile','/api/v1/censuses','/api/v1/censuses?origin=MASIVO','/api/v1/liveness/config']:
            code,body=get(path,jwt)
            assert (code,body.get('body'))==before[(role,path)], 'Datos o permisos distintos tras actualizar'
            assert body['httpCode']==code and body.get('timestamp') and body['message'] not in ['OK','Created']
        print('contrato_y_permisos_'+role+'=OK',flush=True)
    assert query('SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM censuses;')==before_counts
    assert hashlib.sha256((root/'.env').read_bytes()).hexdigest()==env_hash
    print('usuarios_y_censos_sin_cambios='+before_counts.replace('\n',','),flush=True)
    print('configuracion_privada_intacta=SI',flush=True)
    print('deployment_api-messages=OK',flush=True)
except Exception:
    print('Verificacion fallida: restaurando solo API/fuentes, nunca la BD.',flush=True)
    with tarfile.open(backup/'source-before.tar.gz') as archive:archive.extractall(root,filter='data')
    run(['docker','tag',rollback,'mobile-backend-api:latest'])
    compose('up','-d','--no-deps','--no-build','api')
    ready()
    print('rollback_api=OK',flush=True)
    raise
