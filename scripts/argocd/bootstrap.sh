#!/bin/bash
# Registra o Argo CD como gerente do cluster: aplica o root-app (app of apps),
# que cria as Applications de argocd/applications/ e passa a sincronizar k8s/ da master.
set -e
set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

kubectl apply -n argocd -f "$ARGOCD_DIR/root-app.yaml"
kubectl get applications -n argocd
