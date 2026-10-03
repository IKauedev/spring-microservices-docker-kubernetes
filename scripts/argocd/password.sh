#!/bin/bash
# Imprime a senha inicial do usuario admin do Argo CD.

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster >/dev/null

kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath='{.data.password}' | base64 -d
echo
