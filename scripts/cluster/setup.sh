#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

# namespaces, ClusterRole e ClusterRoleBindings (k8s/platform).
# As ServiceAccounts ficam junto de cada app, em k8s/<app>/serviceaccount.yaml.
kubectl apply -k "$K8S_DIR/platform"
