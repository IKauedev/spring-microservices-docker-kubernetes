#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

http $(minikube service employee --url -n $NAMESPACE_EMPLOYEE)/actuator/info