#!/bin/bash
# Estado de sync/saude de todas as Applications.

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster >/dev/null

kubectl get applications -n argocd
