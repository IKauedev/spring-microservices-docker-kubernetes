#!/bin/bash

set -e
set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

minikube start -p $CLUSTER1_NAME --memory="$MINIKUBE_MEMORY" --cpus="$MINIKUBE_CPUS" --disk-size=40g --driver="$MINIKUBE_DRIVER" --insecure-registry=localhost:5000
minikube profile $CLUSTER1_NAME
minikube addons enable ingress
minikube addons enable metrics-server

kubectl config use-context $CLUSTER1_NAME
