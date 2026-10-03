#!/bin/bash

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

# With the docker driver on Windows/macOS the node IP is not reachable from the host,
# so `minikube service --url` opens a tunnel and keeps running: leave this terminal open.
echo "Swagger UI (employee) via gateway: <url printed below>/employee/swagger-ui/index.html"

open_url() {
  case "$(uname -s)" in
    Linux*)  xdg-open "$1" ;;
    Darwin*) open "$1" ;;
    *)       start "" "$1" ;;
  esac
}

# --url prints the tunnel URL on the first line; open it once it is available
minikube service gateway -n $NAMESPACE_GATEWAY -p $CLUSTER1_NAME --url | while read -r url; do
  echo "$url"
  open_url "$url/employee/swagger-ui/index.html"
done
