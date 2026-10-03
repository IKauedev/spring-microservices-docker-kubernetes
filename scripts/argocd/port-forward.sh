#!/bin/bash
# Interface web do Argo CD em https://localhost:8443 (deixe o terminal aberto).
# Usuario: admin  |  senha: ./argocd/password.sh

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

kubectl port-forward -n argocd svc/argocd-server "${1:-8443}:443"
