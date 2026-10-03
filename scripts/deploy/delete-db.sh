#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

kubectl delete -k "$K8S_DIR/mongodb"
