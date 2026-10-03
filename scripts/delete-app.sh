#!/bin/bash

set -x

. ./set-env.sh

kubectl config set-context $CLUSTER1_NAME
kubectl config use-context $CLUSTER1_NAME

for app in department gateway organization employee; do
  kubectl delete -k ../k8s/$app
done
