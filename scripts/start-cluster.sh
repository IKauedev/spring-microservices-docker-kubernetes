#!/bin/bash

set -e
set -x

. ./set-env.sh

minikube start -p $CLUSTER1_NAME --memory='6000mb' --cpus=4 --disk-size=40g --driver="$MINIKUBE_DRIVER" --insecure-registry=localhost:5000
minikube profile $CLUSTER1_NAME
minikube addons enable ingress
minikube addons enable metrics-server

kubectl config use-context $CLUSTER1_NAME
