[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1%20Oakwood-6DB33F)](https://spring.io/projects/spring-cloud)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)

# spring-microservices-docker-kubernetes

A small reference stack of four Spring Boot microservices — **Employee**, **Department**, **Organization** and an edge **Gateway** — wired together with Spring Cloud, backed by MongoDB, and packaged to run on Kubernetes.

It exists mainly as a hands-on playground for the plumbing every microservice fleet needs: service discovery, client-side load balancing, externalized config, health probes, metrics, tracing and API docs — without any real business logic getting in the way.

## Architecture

```mermaid
flowchart LR
    Client(["client"]) --> Gateway["gateway-service\n(Spring Cloud Gateway)"]
    Gateway -- "/employee/**" --> Employee["employee-service"]
    Gateway -- "/department/**" --> Department["department-service"]
    Gateway -- "/organization/**" --> Organization["organization-service"]
    Department -- "Feign: employee" --> Employee
    Organization -- "Feign: department" --> Department
    Organization -- "Feign: employee" --> Employee
    Employee --> Mongo[("MongoDB")]
    Department --> Mongo
    Organization --> Mongo

    subgraph K8s["Kubernetes cluster"]
      Gateway
      Employee
      Department
      Organization
      Mongo
    end
```

Every service registers itself and discovers its peers through the Kubernetes API (`spring-cloud-kubernetes-fabric8`) — there is no Eureka, Consul or Zookeeper in this stack. The gateway routes requests purely by service name via Spring Cloud Gateway's discovery locator, so adding a fifth service to the cluster requires no gateway configuration change.

## Services

| Service               | Role                                            | Talks to                     | Docs (once running)         |
|-----------------------|--------------------------------------------------|-------------------------------|------------------------------|
| `gateway-service`     | Edge router / reverse proxy                      | all of the below (discovery)  | `/actuator`                  |
| `employee-service`    | CRUD for employees, backed by MongoDB            | —                              | `/swagger-ui.html`           |
| `department-service`  | CRUD for departments; enriches with employee data| `employee-service` (Feign)     | `/swagger-ui.html`           |
| `organization-service`| CRUD for organizations; enriches with dept/employee data | `department-service`, `employee-service` (Feign) | `/swagger-ui.html` |

All four expose Spring Boot Actuator on the same port as the app (`health`, `info`, `metrics`, `prometheus`).

## Tech stack

- **Runtime**: Java 17, Spring Boot 4.1.1, Spring Cloud 2025.1.2
- **Discovery & routing**: `spring-cloud-starter-kubernetes-fabric8-all`, Spring Cloud Gateway, Spring Cloud LoadBalancer, Spring Cloud OpenFeign
- **Persistence**: MongoDB (`spring-boot-starter-data-mongodb`)
- **Observability**: Micrometer + Prometheus registry, Micrometer Tracing (Brave bridge)
- **API docs**: springdoc-openapi (OpenAPI 3 / Swagger UI)
- **Packaging**: layered Spring Boot JARs on distroless Java 17 base images
- **Orchestration**: Kubernetes manifests live in the [`spring-microservices-gitops`](https://github.com/IKauedev/spring-microservices-gitops) repository (single source of truth), driven by the shell scripts under [`scripts/`](scripts)

## Repository layout

```
.
├── employee-service/       # Spring Boot app + Dockerfile
├── department-service/     # Spring Boot app + Dockerfile
├── organization-service/   # Spring Boot app + Dockerfile
├── gateway-service/        # Spring Boot app + Dockerfile
├── scripts/
│   ├── lib/                # env.sh (nomes, namespaces) + common.sh (caminhos, use_cluster)
│   ├── cluster/            # start, setup, stop, destroy, ip
│   ├── deploy/             # build-app, build-push, install-*, delete-*
│   ├── ops/                # logs, exec, populate-data, gateway-open
│   └── argocd/             # bootstrap, port-forward, password, status
└── pom.xml                 # Reactor parent (aggregates the four modules)
```

## Prerequisites

- Docker
- [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) + a hypervisor (e.g. VirtualBox)
- `kubectl`
- JDK 17 (via [sdkman](https://sdkman.io/install): `sdk install java 17.0.16-tem`)
- Apache Maven
- `curl` / [HTTPie](https://httpie.org/) for the sample requests below

## Build

```bash
git clone git@github.com:IKauedev/spring-microservices-docker-kubernetes.git
cd spring-microservices-docker-kubernetes
mvn clean package
```

This builds and tests all four modules and produces a layered, executable JAR per service under each `target/` directory.

## CI

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on every push to `master` and on pull requests:

| Job | What it does |
|---|---|
| `build-test` | `mvn verify` for each service in parallel (JDK 17), uploading the Surefire reports |
| `lint` | `bash -n` + `shellcheck` on `scripts/**/*.sh` and `docker compose config` |
| `smoke-test` | `docker compose up --build`, then [`scripts/ci/smoke-test.sh`](scripts/ci/smoke-test.sh) exercises the API through the gateway (employee → department → organization, including the Feign call) |

The smoke test also runs against a cluster: `kubectl port-forward -n gateway svc/gateway 8080:8080` and `./scripts/ci/smoke-test.sh`. Manifests are validated in the [gitops repository](https://github.com/IKauedev/spring-microservices-gitops).

## Run on Kubernetes

The `scripts/` directory wraps the whole lifecycle around a dedicated Minikube profile:

```bash
cd scripts/
./cluster/start.sh        # boot the Minikube profile (MINIKUBE_MEMORY=12000mb, MINIKUBE_CPUS=6 by default)
./cluster/setup.sh        # namespaces and RBAC (k8s/platform)
./deploy/install-all.sh   # build images and apply each app with kubectl apply -k
./ops/populate-data.sh    # seed sample employees/departments/organizations
./ops/gateway-open.sh     # open the Swagger UI through the gateway
```

Tear down with:

```bash
./deploy/delete-all.sh    # remove the app's k8s resources
./cluster/destroy.sh      # remove namespaces/RBAC
./cluster/stop.sh         # stop the Minikube profile
```

`./ops/employee-log.sh`, `./ops/department-log.sh`, `./ops/organization-log.sh` and `./ops/gateway-log.sh` tail a given service's pod logs.

The scripts locate the repository on their own (via `scripts/lib/common.sh`), so they work from any directory.

### GitOps with Argo CD

The cluster configuration (`k8s/` and `argocd/`) lives **only** in [`spring-microservices-gitops`](https://github.com/IKauedev/spring-microservices-gitops); this repository holds the code. The scripts clone it next to this folder on first use (override with `GITOPS_DIR=/path`). With Argo CD installed in the cluster, `./argocd/bootstrap.sh` applies its `argocd/root-app.yaml`; it creates one Application per folder in `k8s/` and keeps the cluster in sync with that repository's `master`. To change a manifest, edit and push it there. Manifests are Kustomize `base/` + `overlays/<env>` (`dev` by default; `ENV_NAME=prod ./deploy/install-all.sh` picks another one), and an `ApplicationSet` generates one Argo Application per environment and app — see that repository's README to add a cluster.

```bash
./argocd/bootstrap.sh      # register the root app (app of apps)
./argocd/status.sh         # sync/health of every Application
./argocd/port-forward.sh   # UI at https://localhost:8443 (user: admin)
./argocd/password.sh       # initial admin password
```

## Run locally with Docker Compose

For a quick, cluster-free way to run the stack, [`docker-compose.yml`](docker-compose.yml) builds and runs
the four apps plus MongoDB with plain Docker networking:

```bash
docker compose up --build
```

| Service                | URL                    |
|-------------------------|-------------------------|
| Gateway                 | http://localhost:8080  |
| Employee (direct)       | http://localhost:8081  |
| Department (direct)     | http://localhost:8082  |
| Organization (direct)   | http://localhost:8083  |
| MongoDB                 | localhost:27017        |

Kubernetes service discovery isn't available outside a cluster, so this file disables
`spring.cloud.kubernetes` and wires the same routing statically instead: Feign clients get their
target services from Spring Cloud's Simple Discovery Client, and the gateway gets a fixed
route per service instead of the discovery locator used in `k8s/gateway/configmap.yaml` of the gitops repo.

## Talking to the API

Once deployed, each service is reachable directly (`minikube service <name> --url -n <namespace>`) or through the gateway. A couple of examples against `employee-service`:

```bash
# create
curl -X POST "$EMPLOYEE_URL/" -H "Content-Type: application/json" \
  -d '{"id":"1","name":"Smith","age":25,"position":"engineer","departmentId":1,"organizationId":1}'

# read
curl "$EMPLOYEE_URL/"
```

See [`scripts/populate-data.sh`](scripts/populate-data.sh) for the full set of sample payloads across employee, department and organization.

## Observability

- **Health / readiness / liveness**: `GET /actuator/health` (wired into the Kubernetes probes in each `k8s/<app>/deployment.yaml` of the gitops repo)
- **Metrics**: `GET /actuator/prometheus` (Micrometer's Prometheus registry)
- **Tracing**: request-scoped trace/span IDs via Micrometer Tracing, correlated in the log pattern configured in each `k8s/<app>/configmap.yaml` of the gitops repo
- **API docs**: `GET /swagger-ui.html` and `GET /v3/api-docs` on each of the three domain services

## License

MIT — see [LICENSE](LICENSE). The original copyright notice from the upstream project is preserved alongside this fork's, as required by the MIT terms.
