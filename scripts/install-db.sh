#!/bin/bash

# set -x

set -x

. ./set-env.sh

kubectl config use-context $CLUSTER1_NAME

# o namespace vem do kustomization.yaml de cada pasta
kubectl apply -k ../k8s/mongodb
