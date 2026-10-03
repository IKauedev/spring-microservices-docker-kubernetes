#!/bin/bash

set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

REPO_USER=${1:-andriykalashnykov}
IS_DEBUG_VERSION=${2:-true}
APP_VER=${3:-1.2}

DEBUG_IMAGE_SUFFIX=""
DEBUG_DOCKERFILE_NAME="Dockerfile"


if [ "$IS_DEBUG_VERSION" == "true" ]; then
    DEBUG_IMAGE_SUFFIX="-debug"
    DEBUG_DOCKERFILE_NAME="Dockerfile.debug"
fi

docker login

cd "$REPO_ROOT/employee-service"
docker build -f ${DEBUG_DOCKERFILE_NAME} -t vmware/employee${DEBUG_IMAGE_SUFFIX}:$APP_VER .
docker tag vmware/employee${DEBUG_IMAGE_SUFFIX}:$APP_VER $REPO_USER/employee${DEBUG_IMAGE_SUFFIX}:$APP_VER
docker push $REPO_USER/employee${DEBUG_IMAGE_SUFFIX}:$APP_VER

cd "$REPO_ROOT/department-service"
docker build -f ${DEBUG_DOCKERFILE_NAME} -t vmware/department${DEBUG_IMAGE_SUFFIX}:$APP_VER .
docker tag vmware/department${DEBUG_IMAGE_SUFFIX}:$APP_VER $REPO_USER/department${DEBUG_IMAGE_SUFFIX}:$APP_VER
docker push $REPO_USER/department${DEBUG_IMAGE_SUFFIX}:$APP_VER

cd "$REPO_ROOT/organization-service"
docker build -f ${DEBUG_DOCKERFILE_NAME} -t vmware/organization${DEBUG_IMAGE_SUFFIX}:$APP_VER .
docker tag vmware/organization${DEBUG_IMAGE_SUFFIX}:$APP_VER $REPO_USER/organization${DEBUG_IMAGE_SUFFIX}:$APP_VER
docker push $REPO_USER/organization${DEBUG_IMAGE_SUFFIX}:$APP_VER

cd "$REPO_ROOT/gateway-service"
docker build -f ${DEBUG_DOCKERFILE_NAME} -t vmware/gateway${DEBUG_IMAGE_SUFFIX}:$APP_VER .
docker tag vmware/gateway${DEBUG_IMAGE_SUFFIX}:$APP_VER $REPO_USER/gateway${DEBUG_IMAGE_SUFFIX}:$APP_VER
docker push $REPO_USER/gateway${DEBUG_IMAGE_SUFFIX}:$APP_VER


docker images