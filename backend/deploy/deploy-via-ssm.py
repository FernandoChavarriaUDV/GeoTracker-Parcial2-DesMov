"""Run in the account owner's CloudShell. Uploads a secret-free bundle and uses SSM."""
import datetime, hashlib, json, pathlib, shlex, subprocess, time, uuid

here=pathlib.Path(__file__).resolve().parent
label_file=here/'deployment-label.txt'
expected_marker='deployment_'+(label_file.read_text().strip() if label_file.exists() else '1.5')+'=OK'
def aws(*args):
    return subprocess.check_output(['aws',*args,'--region','us-east-1','--no-cli-pager'],text=True).strip()
assert json.loads(aws('sts','get-caller-identity'))['Account']=='657498838364', 'Wrong AWS account'
bundle=here/'backend-update.tar.gz'
expected=(here/'backend-update.sha256').read_text().split()[0]
assert hashlib.sha256(bundle.read_bytes()).hexdigest()==expected, 'Package checksum mismatch'
info=json.loads(aws('ssm','describe-instance-information','--filters','Key=InstanceIds,Values=i-01234f8889958550d'))
assert info['InstanceInformationList'] and info['InstanceInformationList'][0]['PingStatus']=='Online', 'SSM offline'
bucket=aws('cloudformation','describe-stack-resource','--stack-name','aws-sam-cli-managed-default','--logical-resource-id','SamCliSourceBucket','--query','StackResourceDetail.PhysicalResourceId','--output','text')
assert bucket and bucket!='None', 'SAM artifact bucket not found'
key='backend-updates/1.5-'+str(uuid.uuid4())+'.tar.gz'
s3='s3://'+bucket+'/'+key
stamp=datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
stage='/opt/mobile-backend-update-'+stamp
try:
    aws('s3','cp',str(bundle),s3,'--sse','AES256','--only-show-errors')
    url=aws('s3','presign',s3,'--expires-in','900')
    # Temporary URL is captured in memory, not printed. No static credentials reach EC2.
    command='set -eu; umask 077; install -d -m 700 '+stage+'; curl --fail --silent --show-error '+shlex.quote(url)+' -o '+stage+'/update.tar.gz; echo '+shlex.quote(expected+'  '+stage+'/update.tar.gz')+' | sha256sum -c -; tar -xzf '+stage+'/update.tar.gz -C '+stage+'; python3 '+stage+'/deploy/apply-update.py'
    result=json.loads(aws('ssm','send-command','--instance-ids','i-01234f8889958550d','--document-name','AWS-RunShellScript','--comment','Backend 1.5 backup, deploy and smoke test','--parameters',json.dumps({'commands':[command],'executionTimeout':['900']})))
    command_id=result['Command']['CommandId']
    print('SSM command: '+command_id,flush=True)
    for _ in range(96):
        time.sleep(10)
        try: result=json.loads(aws('ssm','get-command-invocation','--command-id',command_id,'--instance-id','i-01234f8889958550d'))
        except subprocess.CalledProcessError: continue
        status=result['Status']
        print('Estado: '+status,flush=True)
        if status in ['Success','Cancelled','TimedOut','Failed','Undeliverable','Terminated','InvalidPlatform','AccessDenied']:
            print(result.get('StandardOutputContent',''))
            print(result.get('StandardErrorContent',''))
            if status!='Success' or expected_marker not in result.get('StandardOutputContent',''):
                raise SystemExit('Revisar resultado. No ejecutar de nuevo sin comprobar el estado y el respaldo.')
            break
    else: raise SystemExit('Consulta el comando SSM antes de repetir el despliegue: '+command_id)
finally:
    aws('s3','rm',s3,'--only-show-errors')
    print('Objeto temporal del paquete retirado de S3.',flush=True)
