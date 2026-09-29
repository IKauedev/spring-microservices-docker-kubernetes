#!/bin/bash

# set -e
set -x

. ./set-env.sh


#minikube addons enable ingress

cd ../k8s

kubectl config use-context $CLUSTER1_NAME
# each app's manifests (ConfigMap, Secret, Deployment+Service, ...) now live
# together under k8s/<app>/, so the whole folder is applied in one shot
kubectl apply -n $NAMESPACE_DEPARTMENT -f department/
kubectl apply -n $NAMESPACE_ORGANIZATION -f organization/
kubectl apply -n $NAMESPACE_GATEWAY -f gateway/
kubectl apply -n $NAMESPACE_EMPLOYEE -f employee/

# set Minikupe IP for microservices-cluster.info in /etc/hosts
minikube profile $CLUSTER1_NAME
CLUSTER1_IP=$(minikube ip)
echo $CLUSTER1_IP
sudo sed -i.bak 's/.*microservices-cluster.info/'"$CLUSTER1_IP"' microservices-cluster.info/' /etc/hosts && sudo rm /etc/hosts.bak
echo "$(minikube ip) microservices-cluster.info" | sudo tee -a /etc/hosts

cd ../scripts
