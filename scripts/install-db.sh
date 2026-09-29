#!/bin/bash

# set -e
set -x

. ./set-env.sh

cd ../k8s

kubectl config use-context $CLUSTER1_NAME

kubectl apply -n $NAMESPACE_MONGO -f mongodb/

cd ../scripts