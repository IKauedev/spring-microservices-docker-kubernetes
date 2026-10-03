#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

. "$SCRIPTS_DIR/deploy/delete-app.sh"

. "$SCRIPTS_DIR/deploy/delete-db.sh"

#eval $(minikube docker-env)
#docker rmi $(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'vmware/department:1.1')
#docker rmi $(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'vmware/employee:1.1')
#docker rmi $(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'vmware/gateway:1.1')
#docker rmi $(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'vmware/organization:1.1')