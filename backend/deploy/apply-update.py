"""Run as root on the existing EC2. Preserves DB/secrets and rolls API back on failure."""
import datetime, hashlib, json, os, pathlib, shutil, subprocess, sys, tarfile, time, urllib.request, urllib.error

os.umask(0o077)
stage=pathlib.Path(__file__).resolve().parent.parent
root=pathlib.Path('/opt/mobile-backend')
assert os.geteuid()==0 and root.is_dir() and (root/'.env').is_file()
assert stage!=root and str(stage).startswith('/opt/mobile-backend-update-')
stamp=datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
backup=root/'backups'/('update-1.5-'+stamp)
backup.mkdir(mode=0o700,parents=True)
def run(args,**kwargs):
    return subprocess.run(args,check=True,cwd=root,**kwargs)
def compose(*args):
    return run(['docker','compose','--env-file','.env','-f','docker-compose.prod.yml',*args])
def counts():
    command='MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot "$MYSQL_DATABASE" -Nse "SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM censuses;"'
    return subprocess.check_output(['docker','exec','mobile-backend-mysql-1','sh','-c',command],text=True).strip()
def ready():
    for _ in range(75):
        try:
            urllib.request.urlopen('http://127.0.0.1:8080/api/v1/liveness/config',timeout=2)
        except urllib.error.HTTPError as error:
            if error.code==401: return
        except (OSError,TimeoutError): pass
        time.sleep(2)
    raise RuntimeError('API readiness timeout')

allowed=['pom.xml','Dockerfile','Dockerfile.runtime','Dockerfile.runtime.dockerignore','docker-compose.prod.yml','src','deploy','target']
manifest=json.loads((stage/'manifest.json').read_text())
for rel,expected in manifest.items():
    p=stage/rel
    assert not pathlib.PurePosixPath(rel).is_absolute() and '..' not in pathlib.PurePosixPath(rel).parts
    assert p.is_file() and not p.is_symlink()
    assert hashlib.sha256(p.read_bytes()).hexdigest()==expected, 'Checksum mismatch'
print('checksums=OK',flush=True)
with tarfile.open(backup/'source.tar.gz','w:gz') as archive:
    for item in ['.env',*allowed]:
        p=root/item
        if p.exists() and item!='target': archive.add(p,arcname=item)
dump='MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -uroot --single-transaction --routines --triggers --events --set-gtid-purged=OFF "$MYSQL_DATABASE"'
with (backup/'database.sql').open('wb') as output:
    run(['docker','exec','mobile-backend-mysql-1','sh','-c',dump],stdout=output)
assert (backup/'database.sql').stat().st_size>1000
before=counts()
old_image=subprocess.check_output(['docker','inspect','--format','{{.Image}}','mobile-backend-api-1'],text=True).strip()
rollback='mobile-backend-api:rollback-'+stamp
run(['docker','tag',old_image,rollback])
(backup/'rollback-image.txt').write_text(rollback+'\n')
print('backup='+str(backup),flush=True)
# Build while the previous API keeps serving. No Maven build competes with MySQL RAM.
with (backup/'build.log').open('w') as output:
    try:
        run(['docker','build','-f',str(stage/'Dockerfile.runtime'),'-t','mobile-backend-api:1.5-'+stamp,str(stage)],stdout=output,stderr=subprocess.STDOUT)
    except subprocess.CalledProcessError:
        print('\n'.join((backup/'build.log').read_text().splitlines()[-40:]),flush=True)
        raise
changed=False
try:
    changed=True
    for name in allowed:
        source=stage/name
        if source.is_dir(): shutil.copytree(source,root/name,dirs_exist_ok=True)
        elif source.is_file(): shutil.copy2(source,root/name)
    updates=json.loads((stage/'deploy/aws-public-config.json').read_text())
    obsolete={'LIVENESS_LAMBDA_FUNCTION','AWS_ACCESS_KEY_ID','AWS_SECRET_ACCESS_KEY','AWS_SESSION_TOKEN'}
    lines=[]
    for line in (root/'.env').read_text().splitlines():
        key=line.split('=',1)[0].strip()
        if key not in updates and key not in obsolete: lines.append(line)
    lines.extend(key+'='+value for key,value in updates.items())
    (root/'.env').write_text('\n'.join(lines)+'\n')
    (root/'.env').chmod(0o600)
    compose('config','--quiet')
    run(['docker','tag','mobile-backend-api:1.5-'+stamp,'mobile-backend-api:latest'])
    compose('up','-d','--no-deps','--no-build','api')
    ready()
    print('counts_before='+before.replace('\n',','),flush=True)
    print('counts_after='+counts().replace('\n',','),flush=True)
    run(['python3',str(root/'deploy/verify-deployment.py')])
    run(['nginx','-t'])
    print('deployment_1.5=OK',flush=True)
except Exception:
    print('Update verification failed. Restoring previous API; DB is NOT overwritten.',flush=True)
    if changed:
        with tarfile.open(backup/'source.tar.gz') as archive:
            archive.extractall(root,filter='data')
        run(['docker','tag',rollback,'mobile-backend-api:latest'])
        compose('up','-d','--no-deps','--no-build','api')
        ready()
        print('rollback_api=OK',flush=True)
    raise
