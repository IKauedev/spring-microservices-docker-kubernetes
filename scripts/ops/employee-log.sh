#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

kubectl get pod -n $NAMESPACE_EMPLOYEE -l 'app=employee' --no-headers | awk '{print $1}' | xargs -I {} sh -c 'echo {}; kubectl logs --follow {} -n $NAMESPACE_EMPLOYEE'
