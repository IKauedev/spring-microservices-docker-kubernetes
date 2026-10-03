#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

minikube profile $CLUSTER1_NAME
minikube ip