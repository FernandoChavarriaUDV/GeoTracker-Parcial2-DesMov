"""Create a reproducible allow-listed deployment artifact, never private configuration."""
import hashlib, io, json, pathlib, tarfile, zipfile, sys

root=pathlib.Path(__file__).resolve().parent.parent
destination=root.parent/'entregables'
destination.mkdir(exist_ok=True)
files=[root/name for name in ['pom.xml','Dockerfile','Dockerfile.runtime','Dockerfile.runtime.dockerignore','docker-compose.prod.yml',
    'target/mobile-backend-0.0.1-SNAPSHOT.jar','deploy/apply-update.py','deploy/verify-deployment.py','deploy/aws-public-config.json']]
files.extend(p for p in (root/'src').rglob('*') if p.is_file())
messages_only='--api-messages' in sys.argv[1:]
if messages_only:
    files=[p for p in files if p.relative_to(root).as_posix() not in ['docker-compose.prod.yml','deploy/apply-update.py','deploy/verify-deployment.py','deploy/aws-public-config.json']]
    files.append(root/'deploy/apply-api-messages.py')
assert all(p.is_file() for p in files), 'Build the tested JAR first'
def name(p):
    rel=p.relative_to(root).as_posix()
    return 'deploy/apply-update.py' if messages_only and rel=='deploy/apply-api-messages.py' else rel
manifest={name(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
buffer=io.BytesIO()
with tarfile.open(fileobj=buffer,mode='w:gz') as archive:
    for path in files: archive.add(path,arcname=name(path))
    payload=json.dumps(manifest,indent=2).encode()
    item=tarfile.TarInfo('manifest.json'); item.size=len(payload)
    archive.addfile(item,io.BytesIO(payload))
data=buffer.getvalue()
sha=hashlib.sha256(data).hexdigest()
out=destination/('backend-mensajes-api-cloudshell.zip' if messages_only else 'backend-1.5-cloudshell.zip')
with zipfile.ZipFile(out,'w',compression=zipfile.ZIP_STORED) as archive:
    archive.writestr('backend-update.tar.gz',data)
    archive.writestr('backend-update.sha256',sha+'  backend-update.tar.gz\n')
    archive.write(root/'deploy/deploy-via-ssm.py','deploy-via-ssm.py')
    if messages_only:archive.writestr('deployment-label.txt','api-messages')
print('Package: '+str(out))
print('Bytes: '+str(out.stat().st_size))
print('Inner SHA256: '+sha)
print('Allow-listed source files: '+str(len(files)))
