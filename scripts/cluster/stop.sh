#!/bin/bash

set -e
set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

minikube stop -p $CLUSTER1_NAME
