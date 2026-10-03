#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

. "$SCRIPTS_DIR/deploy/delete-all.sh"

use_cluster

# ClusterRole, ClusterRoleBindings e namespaces (as ServiceAccounts somem com os namespaces)
kubectl delete -k "$OVERLAY_DIR/platform"
