#!/bin/bash
# Teste de fumaca ponta a ponta atraves do gateway.
#   ./scripts/ci/smoke-test.sh [BASE_URL]     (padrao: http://localhost:8080)
# Sobe com `docker compose up -d --build` (CI) ou com um port-forward do gateway no cluster.
# Cria dados com IDs unicos, entao pode rodar varias vezes sem limpar o banco.

BASE_URL="${1:-http://localhost:8080}"
WAIT_SECONDS="${WAIT_SECONDS:-240}"
RUN_ID="$(date +%s)"   # numerico: os modelos usam Long em departmentId/organizationId
FAILED=0

pass() { echo "  ok   $1"; }
fail() { echo "  FAIL $1"; FAILED=1; }

# espera uma URL responder 200 com "UP" no corpo
wait_up() {
  local name="$1" url="$2" deadline=$((SECONDS + WAIT_SECONDS))
  until curl -fsS --max-time 5 "$url" 2>/dev/null | grep -q '"status":"UP"'; do
    if [ $SECONDS -ge $deadline ]; then fail "$name nao ficou UP em ${WAIT_SECONDS}s ($url)"; return 1; fi
    sleep 3
  done
  pass "$name UP"
}

# POST/GET JSON: devolve o corpo e confere o status HTTP
request() { # metodo url [corpo]
  local method="$1" url="$2" body="$3"
  if [ -n "$body" ]; then
    curl -sS --max-time 20 -w '\n%{http_code}' -X "$method" "$url" -H 'Content-Type: application/json' -d "$body"
  else
    curl -sS --max-time 20 -w '\n%{http_code}' -X "$method" "$url"
  fi
}

expect() { # descricao "resposta (corpo\nstatus)" padrao_no_corpo
  local desc="$1" resp="$2" pattern="$3"
  local code body
  code=$(echo "$resp" | tail -n1); body=$(echo "$resp" | sed '$d')
  if [ "$code" = "200" ] && echo "$body" | grep -q "$pattern"; then pass "$desc"; else fail "$desc (HTTP $code, esperado /$pattern/): $(echo "$body" | head -c 200)"; fi
}

echo "== saude ($BASE_URL)"
wait_up gateway      "$BASE_URL/actuator/health" || exit 1
wait_up employee     "$BASE_URL/employee/actuator/health" || exit 1
wait_up department   "$BASE_URL/department/actuator/health" || exit 1
wait_up organization "$BASE_URL/organization/actuator/health" || exit 1

echo "== employee"
expect "cria employee" "$(request POST "$BASE_URL/employee/" \
  "{\"id\":\"$RUN_ID\",\"name\":\"Smith\",\"age\":25,\"position\":\"engineer\",\"departmentId\":$RUN_ID,\"organizationId\":$RUN_ID}")" "\"id\":\"$RUN_ID\""
expect "le employee por id" "$(request GET "$BASE_URL/employee/$RUN_ID")" '"name":"Smith"'
expect "lista employees" "$(request GET "$BASE_URL/employee/")" "$RUN_ID"
expect "busca por organizacao" "$(request GET "$BASE_URL/employee/organization/$RUN_ID")" "$RUN_ID"

echo "== department"
expect "cria department" "$(request POST "$BASE_URL/department/" \
  "{\"id\":\"$RUN_ID\",\"name\":\"RD Dept.\",\"organizationId\":$RUN_ID}")" "\"id\":\"$RUN_ID\""
expect "le department por id" "$(request GET "$BASE_URL/department/$RUN_ID")" '"name":"RD Dept."'

echo "== organization (chama employee via Feign)"
expect "cria organization" "$(request POST "$BASE_URL/organization/" \
  "{\"id\":\"$RUN_ID\",\"name\":\"Acme\",\"address\":\"Main Street\"}")" "\"id\":\"$RUN_ID\""
expect "organization com employees (Feign)" "$(request GET "$BASE_URL/organization/$RUN_ID/with-employees")" '"name":"Smith"'

echo
if [ $FAILED -eq 0 ]; then echo "SMOKE TEST OK"; else echo "SMOKE TEST FALHOU"; fi
exit $FAILED
