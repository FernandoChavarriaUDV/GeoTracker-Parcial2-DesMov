#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export AWS_DEFAULT_REGION=us-east-1
account="$(aws sts get-caller-identity --query Account --output text)"
if [[ "$account" != "657498838364" ]]; then
  echo "Cuenta incorrecta: abre CloudShell en FC-ens2026." >&2
  exit 1
fi
command -v sam >/dev/null
command -v npm >/dev/null
npm --prefix node ci --omit=dev --ignore-scripts
sam validate --template-file template.yaml --region us-east-1
echo "Se crearan recursos facturables de Lambda, Cognito y CloudWatch; Rekognition cobra por uso. Revisa el changeset antes de confirmar."
sam deploy --template-file template.yaml --stack-name fc-ens2026-liveness \
  --region us-east-1 --resolve-s3 --capabilities CAPABILITY_IAM \
  --parameter-overrides BackendInstanceRoleName=Backend-DesMov-SSM \
  --confirm-changeset --no-fail-on-empty-changeset
aws cloudformation describe-stacks --stack-name fc-ens2026-liveness \
  --region us-east-1 --query 'Stacks[0].Outputs' --output table --no-cli-pager
