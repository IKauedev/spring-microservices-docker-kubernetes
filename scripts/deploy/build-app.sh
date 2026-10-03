#!/bin/bash

set -e
set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"


minikube profile $CLUSTER1_NAME

# make Kubernetes reusing Docker daemon
# https://kubernetes.io/docs/setup/minikube/#reusing-the-docker-daemon
eval $(minikube docker-env)
docker images

(cd "$REPO_ROOT" && mvn clean)

cd "$REPO_ROOT/department-service"
docker build -t vmware/department:1.1 .

cd "$REPO_ROOT/gateway-service"
docker build -t vmware/gateway:1.1 .

cd "$REPO_ROOT/organization-service"
docker build -t vmware/organization:1.1 .


cd "$REPO_ROOT/employee-service"
docker build -t vmware/employee:1.1 .

docker images

