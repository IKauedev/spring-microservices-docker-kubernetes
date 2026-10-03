#!/bin/bash

# set -x

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

# o namespace vem do kustomization.yaml de cada pasta
kubectl apply -k "$K8S_DIR/mongodb"
