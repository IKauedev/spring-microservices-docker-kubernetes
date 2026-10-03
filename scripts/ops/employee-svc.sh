#!/bin/bash

#set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

# this will only work if Employee Docker image build  from non-distroless image, see employee-exec.sh
kubectl get pod -n $NAMESPACE_EMPLOYEE -l 'app=employee' --no-headers | awk '{print $1}' | xargs -I {} sh -c  "kubectl exec -t {} -n \"$NAMESPACE_EMPLOYEE\" -- cat /etc/resolv.conf"


