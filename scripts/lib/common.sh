#!/bin/bash
# Carregado por todos os scripts:  . "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"
# Define os caminhos do repositorio e as variaveis de lib/env.sh, para que os
# scripts funcionem de qualquer diretorio atual.

SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_ROOT="$(cd "$SCRIPTS_DIR/.." && pwd)"

. "$SCRIPTS_DIR/lib/env.sh"

# Fonte unica da configuracao do cluster: o repositorio spring-microservices-gitops
# (k8s/ e argocd/). Por padrao e um clone ao lado deste repositorio; use
# GITOPS_DIR=/outro/caminho para sobrescrever.
GITOPS_REPO_URL="${GITOPS_REPO_URL:-https://github.com/IKauedev/spring-microservices-gitops.git}"
GITOPS_DIR="${GITOPS_DIR:-$(dirname "$REPO_ROOT")/spring-microservices-gitops}"
if [ ! -d "$GITOPS_DIR/k8s" ]; then
  echo "Clonando $GITOPS_REPO_URL em $GITOPS_DIR"
  git clone "$GITOPS_REPO_URL" "$GITOPS_DIR" || return 1
fi
K8S_DIR="$GITOPS_DIR/k8s"
OVERLAY_DIR="$K8S_DIR/overlays/$ENV_NAME"
ARGOCD_DIR="$GITOPS_DIR/argocd"
export SCRIPTS_DIR REPO_ROOT GITOPS_DIR K8S_DIR OVERLAY_DIR ARGOCD_DIR


# aponta o kubectl para o cluster configurado em env.sh
use_cluster() {
  kubectl config set-context "$CLUSTER1_NAME"
  kubectl config use-context "$CLUSTER1_NAME"
}
