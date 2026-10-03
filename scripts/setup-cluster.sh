#!/bin/bash

set -x

. ./set-env.sh

kubectl config set-context $CLUSTER1_NAME
kubectl config use-context $CLUSTER1_NAME

# namespaces, ClusterRole e ClusterRoleBindings (k8s/platform).
# As ServiceAccounts ficam junto de cada app, em k8s/<app>/serviceaccount.yaml.
kubectl apply -k ../k8s/platform
