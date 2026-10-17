#!/bin/bash
# Rollback via GitOps: reverte o ultimo commit do repositorio gitops (ou o informado)
# e o Argo CD sincroniza o cluster de volta ao estado anterior.
#
# Uso:
#   scripts/deploy/rollback.sh              # mostra os ultimos commits de k8s/
#   scripts/deploy/rollback.sh HEAD         # reverte o ultimo commit e faz push
#   scripts/deploy/rollback.sh <sha>        # reverte um commit especifico
#
# kubectl rollout undo NAO e usado: com selfHeal ligado o Argo CD desfaz o undo,
# porque o Git continua apontando para a versao ruim.
set -e

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster >/dev/null

cd "$GITOPS_DIR"

if [ -z "$1" ]; then
  echo "Ultimos commits em k8s/ (passe um sha ou HEAD para reverter):"
  git log --oneline -n 10 -- k8s
  exit 0
fi

git pull --ff-only
git revert --no-edit "$1"
git push

echo "Revert enviado. Acompanhe a sincronizacao:"
kubectl get applications -n argocd
