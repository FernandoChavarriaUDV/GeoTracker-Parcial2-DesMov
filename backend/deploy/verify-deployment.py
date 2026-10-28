"""Owner-only smoke test. Never prints credentials, tokens or personal data."""
import base64, hashlib, hmac, json, subprocess, time, urllib.request, urllib.error

def query(sql):
    command='MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot "$MYSQL_DATABASE" -Nse "'+sql+'"'
    return subprocess.check_output(['docker','exec','mobile-backend-mysql-1','sh','-c',command],text=True).strip()

config=json.loads(subprocess.check_output(['docker','inspect','mobile-backend-api-1']))[0]
env=dict(item.split('=',1) for item in config['Config']['Env'] if '=' in item)
assert env.get('LIVENESS_ENABLED')=='true', 'Liveness not enabled'
assert not any(env.get(k) for k in ['AWS_ACCESS_KEY_ID','AWS_SECRET_ACCESS_KEY','AWS_SESSION_TOKEN']), 'Use EC2 role, not static keys'
assert query("SELECT COUNT(*) FROM flyway_schema_history WHERE version='5' AND success=1")=='1'

def encode(value): return base64.urlsafe_b64encode(value).rstrip(b'=')
def token(user_id):
    now=int(time.time())
    content=encode(b'{"alg":"HS256","typ":"JWT"}')+b'.'+encode(json.dumps({'sub':str(user_id),'iat':now,'exp':now+120},separators=(',',':')).encode())
    return (content+b'.'+encode(hmac.new(env['JWT_SECRET'].encode(),content,hashlib.sha256).digest())).decode()
def request(path,jwt=None,body=None):
    req=urllib.request.Request('http://127.0.0.1:8080'+path,data=None if body is None else json.dumps(body).encode())
    if jwt: req.add_header('Authorization','Bearer '+jwt)
    req.add_header('Content-Type','application/json')
    try:
        with urllib.request.urlopen(req,timeout=55) as response: return response.status,json.load(response)
    except urllib.error.HTTPError as error: return error.code,json.load(error)

assert request('/api/v1/liveness/config')[0]==401
assert request('/api/v1/admin/users',body={})[0]==401
admin=None
for row in query('SELECT MIN(id),role FROM users GROUP BY role').splitlines():
    user_id,role=row.split()
    jwt=token(user_id)
    status,body=request('/api/v1/liveness/config',jwt)
    assert status==200 and body['body']['configured'] is True
    assert request('/api/v1/liveness/sessions/unknown',jwt)[0]==404
    assert request('/api/v1/admin/users',jwt,{})[0]==(400 if role=='ADMINISTRADOR' else 403)
    assert request('/api/v1/censuses',jwt)[0]==(200 if role=='ADMINISTRADOR' else 403)
    if role=='ADMINISTRADOR': admin=jwt
    print('authorization_'+role+'=OK',flush=True)
assert admin, 'Administrator required for deployment check'
assert request('/api/v1/liveness/sessions',admin,{'consent':False})[0]==400
status,body=request('/api/v1/liveness/sessions',admin,{'consent':True})
assert status==200 or status==201, 'Create session failed: HTTP '+str(status)
session=body['body']
assert session['sessionId'] and session['region']=='us-east-1'
status,body=request('/api/v1/liveness/sessions/'+session['id'],admin)
assert status==200 and body['body']['status'] in ['PROCESSING','CREATED']
assert body['body']['verified'] is False
assert body['body'].get('referenceBase64') is None
print('lambda_create_and_results=OK',flush=True)
print('migration_5=OK',flush=True)
print('smoke=OK',flush=True)
