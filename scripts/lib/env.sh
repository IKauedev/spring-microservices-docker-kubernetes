#!/bin/bash

#set -x

export NAMESPACE_DEPARTMENT=department
export NAMESPACE_EMPLOYEE=employee
export NAMESPACE_GATEWAY=gateway
export NAMESPACE_ORGANIZATION=organization
export NAMESPACE_MONGO=mongo

export SA_NAME=api-service-account

export CLUSTER1_NAME=minikube-cluster-1

# minikube driver (docker works on Windows/macOS/Linux; override with e.g. MINIKUBE_DRIVER=virtualbox)
export MINIKUBE_DRIVER=${MINIKUBE_DRIVER:-docker}
