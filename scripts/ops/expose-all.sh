#!/bin/bash
# Expoe todos os servicos em localhost de uma vez (deixe o terminal aberto; Ctrl+C encerra tudo):
#   gateway      http://localhost:8080   (Swagger: /employee/swagger-ui/index.html)
#   employee     http://localhost:8081
#   department   http://localhost:8082
#   organization http://localhost:8083
#   mongodb      localhost:27017
#   argocd       https://localhost:8443  (usuario: admin | senha: ./scripts/argocd/password.sh)
# Se o pod for recriado (ex.: rollout), o port-forward reconecta sozinho.

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

use_cluster

WORK_DIR="$(mktemp -d)"
pids=()

# porta em LISTEN? (um unico netstat; testar via /dev/tcp leva ~2s por porta fechada no Windows)
LISTENING="$(netstat -an 2>/dev/null | grep -i LISTEN)"
port_busy() { echo "$LISTENING" | grep -qE "[.:]$1[[:space:]]"; }

# mata o kubectl e os filhos dele (no Windows o kubectl.exe e um shim que lanca outro processo)
kill_tree() {
  local winpid
  winpid=$(cat "/proc/$1/winpid" 2>/dev/null)
  if [ -n "$winpid" ]; then taskkill //F //T //PID "$winpid" >/dev/null 2>&1; else kill "$1" 2>/dev/null; fi
}

cleanup() {
  echo; echo "Encerrando port-forwards..."
  for pid in "${pids[@]}"; do kill "$pid" 2>/dev/null; done
  for f in "$WORK_DIR"/*.pid; do [ -f "$f" ] && kill_tree "$(cat "$f")"; done
  rm -rf "$WORK_DIR"
  exit 0
}
trap cleanup INT TERM

# forward <namespace> <servico> <porta-local> <porta-servico>
forward() {
  (
    while true; do
      kubectl port-forward -n "$1" "svc/$2" "$3:$4" >"$WORK_DIR/$2.log" 2>&1 &
      echo $! > "$WORK_DIR/$2.pid"
      wait $!
      sleep 2
    done
  ) &
  pids+=($!)
  echo "$2 -> localhost:$3"
}

# falha logo se alguma porta ja estiver ocupada (ex.: port-forward de uma execucao anterior)
busy=0
for port in 8080 8081 8082 8083 27017 8443; do
  if port_busy "$port"; then echo "ERRO: a porta $port ja esta em uso." >&2; busy=1; fi
done
if [ $busy -eq 1 ]; then
  echo "Encerre o processo que usa a porta (ex.: port-forward de um expose-all.sh anterior; no Windows: taskkill //F //IM kubectl.exe encerra TODOS os kubectl)." >&2
  rm -rf "$WORK_DIR"
  exit 1
fi

forward "$NAMESPACE_GATEWAY"      gateway      8080 8080
forward "$NAMESPACE_EMPLOYEE"     employee     8081 8080
forward "$NAMESPACE_DEPARTMENT"   department   8082 8080
forward "$NAMESPACE_ORGANIZATION" organization 8083 8080
forward "$NAMESPACE_MONGO"        mongodb      27017 27017
forward argocd                    argocd-server 8443 443

echo "Argo CD: https://localhost:8443 (admin / senha: ./scripts/argocd/password.sh)"
echo "Pronto. Ctrl+C para encerrar. Logs de erro: $WORK_DIR/<servico>.log"
wait
