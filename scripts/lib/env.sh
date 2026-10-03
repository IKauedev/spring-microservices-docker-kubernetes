#!/bin/bash

#set -x

export NAMESPACE_DEPARTMENT=department
export NAMESPACE_EMPLOYEE=employee
export NAMESPACE_GATEWAY=gateway
export NAMESPACE_ORGANIZATION=organization
export NAMESPACE_MONGO=mongo

export SA_NAME=api-service-account

# perfil do Minikube e ambiente (overlay do gitops: dev | prod); sobrescreva por variavel de ambiente
export CLUSTER1_NAME=${CLUSTER1_NAME:-minikube-cluster-1}
export ENV_NAME=${ENV_NAME:-dev}

# minikube driver (docker works on Windows/macOS/Linux; override with e.g. MINIKUBE_DRIVER=virtualbox)
export MINIKUBE_DRIVER=${MINIKUBE_DRIVER:-docker}

# recursos do no do Minikube (12 GiB evitam o travamento do API server com ~20 pods JVM)
export MINIKUBE_MEMORY=${MINIKUBE_MEMORY:-12000mb}
export MINIKUBE_CPUS=${MINIKUBE_CPUS:-6}
