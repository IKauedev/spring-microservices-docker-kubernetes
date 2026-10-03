#!/bin/bash

set -x

. ./set-env.sh

. ./delete-all.sh

kubectl config set-context $CLUSTER1_NAME
kubectl config use-context $CLUSTER1_NAME

# ClusterRole, ClusterRoleBindings e namespaces (as ServiceAccounts somem com os namespaces)
kubectl delete -k ../k8s/platform
