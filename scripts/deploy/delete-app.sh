#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

for app in department gateway organization employee; do
  kubectl delete -k "$K8S_DIR/$app"
done
