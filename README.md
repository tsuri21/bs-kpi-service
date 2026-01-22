# KPI Management Tool

This is the backend for the KPI Management System. It is designed to capture, track, and analyze Key Performance Indicators (KPIs) regarding the code status and health of code repositories.

This project was created as part of the Backend Systems module. It is built using Java and Quarkus, following the principles of Hexagonal Architecture (Ports & Adapters) to ensure strict isolation of domain logic.

## Prerequisites

- JDK 21+
- Maven 3.9+ 
- Docker (required for Integration Tests and Container builds)

## Development Mode

To run the application in local development mode with live coding enabled:

```shell 
mvn compile quarkus:dev
```

The API will be available at http://localhost:8080.

## Unit Tests

We differentiate between unit tests and integration tests when running the application.
By default, the standard `mvn package` command executes only unit tests.

To run unit tests directly:
```shell
mvn test
```

## Integration Tests

The integration tests are implemented as black-box tests and treat the application as a fully external system:
- No internal Quarkus mocking is used.
- The application is started inside a Docker container.
- Tests interact with the system exclusively via real HTTP requests.

To run the integration tests, activate the Docker profile. This profile takes care of packaging, container startup, test execution, and teardown:
```shell
mvn verify -P docker
```

## Manual Docker Build & Run

If you want to build and run the Docker image manually, please follow these steps carefully.
Important: Since our Dockerfile copies the compiled artifacts from the target/ directory, you must build the project with Maven first.

Package the application:
```shell
mvn package
```

Build the Docker image using the standard JVM Dockerfile:
```shell
docker build -f src/main/docker/Dockerfile.jvm -t kpi-manager:latest .
```

Run the container:
```shell
docker run -i --rm -p 8080:8080 kpi-manager:latest
```
