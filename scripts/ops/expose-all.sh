#!/bin/bash
# Expoe todos os servicos em localhost de uma vez (deixe o terminal aberto; Ctrl+C encerra tudo):
#   gateway      http://localhost:8080   (Swagger: /employee/swagger-ui/index.html)
#   employee     http://localhost:8081
#   department   http://localhost:8082
#   organization http://localhost:8083
#   mongodb      localhost:27017
# Se o pod for recriado (ex.: rollout), o port-forward reconecta sozinho.

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

pids=()
trap 'echo; echo "Encerrando port-forwards..."; kill "${pids[@]}" 2>/dev/null; exit 0' INT TERM

# forward <namespace> <servico> <porta-local> <porta-servico>
forward() {
  (
    while true; do
      kubectl port-forward -n "$1" "svc/$2" "$3:$4" >/dev/null 2>&1
      sleep 2
    done
  ) &
  pids+=($!)
  echo "$2 -> localhost:$3"
}

forward "$NAMESPACE_GATEWAY"      gateway      8080 8080
forward "$NAMESPACE_EMPLOYEE"     employee     8081 8080
forward "$NAMESPACE_DEPARTMENT"   department   8082 8080
forward "$NAMESPACE_ORGANIZATION" organization 8083 8080
forward "$NAMESPACE_MONGO"        mongodb      27017 27017

echo "Pronto. Ctrl+C para encerrar."
wait
