#!/bin/bash
# Carregado por todos os scripts:  . "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"
# Define os caminhos do repositorio e as variaveis de lib/env.sh, para que os
# scripts funcionem de qualquer diretorio atual.

SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_ROOT="$(cd "$SCRIPTS_DIR/.." && pwd)"
K8S_DIR="$REPO_ROOT/k8s"
ARGOCD_DIR="$REPO_ROOT/argocd"
export SCRIPTS_DIR REPO_ROOT K8S_DIR ARGOCD_DIR

. "$SCRIPTS_DIR/lib/env.sh"

# aponta o kubectl para o cluster configurado em env.sh
use_cluster() {
  kubectl config set-context "$CLUSTER1_NAME"
  kubectl config use-context "$CLUSTER1_NAME"
}
