#!/bin/bash

# set -e
set -x

. ./set-env.sh

kubectl config use-context $CLUSTER1_NAME

# cada app tem um kustomization.yaml (namespace + recursos); -k aplica a pasta inteira
for app in department organization gateway employee; do
  kubectl apply -k ../k8s/$app
done

minikube profile $CLUSTER1_NAME
CLUSTER1_IP=$(minikube ip)
echo $CLUSTER1_IP

# map microservices-cluster.info to the Minikube IP in /etc/hosts (Linux/macOS only;
# on Windows / docker driver use `minikube service gateway -n gateway --url` instead,
# see gateway-open.sh)
case "$(uname -s)" in
  Linux*|Darwin*)
    sudo sed -i.bak 's/.*microservices-cluster.info/'"$CLUSTER1_IP"' microservices-cluster.info/' /etc/hosts && sudo rm /etc/hosts.bak
    echo "$CLUSTER1_IP microservices-cluster.info" | sudo tee -a /etc/hosts
    ;;
  *)
    echo "Skipping /etc/hosts update on this OS; use gateway-open.sh to reach the gateway."
    ;;
esac
