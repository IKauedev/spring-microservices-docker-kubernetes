[![GitHub CI Status](https://github.com/IKauedev/spring-microservices-docker-kubernetes/workflows/ci/badge.svg)](https://github.com/IKauedev/spring-microservices-docker-kubernetes/actions?query=workflow%3Aci)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FIKauedev%2Fspring-microservices-docker-kubernetes&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
### Java Microservices with Spring Boot and Spring Cloud Kubernetes

This repository is a fork of Andriy Kalashnykov's [spring-microservices-k8s](https://github.com/AndriyKalashnykov/spring-microservices-k8s), which accompanies the `Tanzu Development Center` article - [Microservices with Spring Cloud Kubernetes Reference Architecture](https://tanzu.vmware.com/developer/guides/app-enhancements-spring-k8s//). This fork has been modernized to run on **Spring Boot 4.1** and **Spring Cloud 2025.1 (Oakwood)**, replacing dependencies that were discontinued in the original (Netflix Zuul, Netflix Ribbon, Spring Cloud Sleuth, Springfox) with their current equivalents (Spring Cloud Gateway, Spring Cloud LoadBalancer, Micrometer Tracing, springdoc-openapi) and moving to Java 17.

This Reference Architecture demonstrates design, development, and deployment of
[Spring Boot](https://spring.io/projects/spring-boot) microservices on
Kubernetes. Each section covers architectural recommendations and configuration
for each concern when applicable.

High-level key recommendations:

- Consider Best Practices in Cloud Native Applications and [The 12
  Factor App](https://12factor.net/)
- Keep each microservice in a separate [Maven](https://maven.apache.org/) or
  [Gradle](https://docs.gradle.org/current/userguide/userguide.html) project
- Prefer using dependencies when inheriting from parent project instead of using
  relative path
- Use [Spring Initializr](https://start.spring.io/) a web application that can
  generate a Spring Boot project structure, fill in your project details, pick
  your options, and download a bundled up project

This architecture demonstrates a complex Cloud Native application that
addresses following concerns:

- Externalized configuration using ConfigMaps, Secrets, and PropertySource
- Kubernetes API server access using ServiceAccounts, Roles, and RoleBindings
- Health checks using Application Probes
  - readinessProbe
  - livenessProbe
  - startupProbe
- Reporting application state using Spring Boot Actuators
- Service discovery across namespaces using DiscoveryClient
- Exposing API documentation using Swagger UI
- Building a Docker image using best practices
- Layering JARs using the Spring Boot plugin
- Observing the application using Prometheus exporters
### Pre-requisites

- OS: Mac or Linux
- [Docker](https://docs.docker.com/install/)
- [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)
- [Virtualbox](https://www.virtualbox.org/manual/ch02.html)
- [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- [sdkman](https://sdkman.io/install)

    JDK 17.x

    ```shell
    sdk install java 17.0.16-tem
    sdk use java 17.0.16-tem

    ```
- [Apache Maven](https://maven.apache.org/install.html)
- [Curl](https://help.ubidots.com/en/articles/2165289-learn-how-to-install-run-curl-on-windows-macosx-linux)
- [HTTPie](https://httpie.org/doc#installation)
- [tree](http://mama.indstate.edu/users/ice/tree/)

### Clone repository

```bash
git clone git@github.com:IKauedev/spring-microservices-docker-kubernetes.git
```

### Start Kubernetes cluster

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./start-cluster.sh
```

### Configure Kubernetes cluster

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./setup-cluster.sh
```

### Deploy application to Kubernetes cluster

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./install-all.sh
```

### Polulate test data

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./populate-data.sh
```

### Observe Employee service logs

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./employee-log.sh
```

### Open Swagger UI web interface

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./gateway-open.sh
```

### Undeploy application from Kubernetes cluster

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./delete-all.sh
```

### Delete Application specific Kubernetes cluster configuration (namespaces, clusterRole, etc.)

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./destroy-cluster.sh
```

### Stop Kubernetes cluster

```bash
cd ./spring-microservices-docker-kubernetes/scripts/
./stop-cluster.sh
```


## Stargazers over time

[![Stargazers over time](https://starchart.cc/IKauedev/spring-microservices-docker-kubernetes.svg)](https://starchart.cc/IKauedev/spring-microservices-docker-kubernetes)

