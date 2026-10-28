"""Read-only smoke test. Uses a short-lived token without printing credentials."""
import base64
import hashlib
import hmac
import json
import subprocess
import sys
import time
import urllib.error
import urllib.request

user_id = int(sys.argv[1])
config = json.loads(subprocess.check_output(["docker", "inspect", "mobile-backend-api-1"]))[0]
environment = dict(entry.split("=", 1) for entry in config["Config"]["Env"] if "=" in entry)
secret = environment["JWT_SECRET"]

def encoded(value):
    return base64.urlsafe_b64encode(value).rstrip(b"=")

now = int(time.time())
header = encoded(b'{"alg":"HS256","typ":"JWT"}')
payload = encoded(json.dumps({"sub": str(user_id), "iat": now, "exp": now + 60}, separators=(",", ":")).encode())
content = header + b"." + payload
token = (content + b"." + encoded(hmac.new(secret.encode(), content, hashlib.sha256).digest())).decode()

def get(path, authenticated=True):
    request = urllib.request.Request("http://127.0.0.1:8080" + path)
    if authenticated:
        request.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(request, timeout=15) as response:
            return response.status, json.load(response)
    except urllib.error.HTTPError as error:
        return error.code, json.load(error)

status, profile = get("/api/v1/profile")
role = profile.get("body", {}).get("role")
print(json.dumps({"profile_status": status, "role": role}))
assert status == 200 and role in ("CENSISTA", "ADMINISTRADOR"), "Profile role missing"
status, _ = get("/api/v1/censuses")
print(json.dumps({"censuses_status": status, "expected": 200 if role == "ADMINISTRADOR" else 403}))
assert status == (200 if role == "ADMINISTRADOR" else 403), "Role authorization failed"
status, _ = get("/api/v1/censuses", authenticated=False)
print(json.dumps({"unauthenticated_status": status}))
assert status == 401, "Unauthenticated request not rejected"
