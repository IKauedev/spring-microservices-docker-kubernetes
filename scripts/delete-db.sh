#!/bin/bash

set -x

. ./set-env.sh

kubectl config set-context $CLUSTER1_NAME
kubectl config use-context $CLUSTER1_NAME

kubectl delete -k ../k8s/mongodb
