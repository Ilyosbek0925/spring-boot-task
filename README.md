# Spring Boot Homework

This project contains a set of Spring Boot applications and features implemented as part of the homework assignments.

## Tasks

### Task 1 – Hello World Application
- Create a basic Spring Boot application using Spring Initializr.
- Implement `CommandLineRunner`.
- Print `hello world` to the console when the application starts.

### Task 2 – CRUD REST Application
- Create a simple entity with basic attributes.
- Implement data access using Spring Data JPA.
- Develop REST endpoints for Create, Read, Update, and Delete operations.
- Test the endpoints using Postman or cURL.

### Task 3 – Security
- Implement authentication and authorization using OAuth2 and JWT.
- Configure JWT token generation and validation.
- Restrict endpoint access based on user roles, such as `USER` and `ADMIN`.

### Task 4 – Externalized Configuration (Optional)
- Configure Spring Profiles for `local`, `dev`, `stg`, and `prod` environments.
- Create separate `application-{profile}.yml` configuration files.
- Configure environment-specific database properties.

### Task 5 – Database Migration (Optional)
- Integrate Flyway or Liquibase.
- Create database migration scripts.
- Configure automatic migrations during application startup.

### Task 6 – Actuator
- Integrate Spring Boot Actuator.
- Implement custom health indicators.
- Add custom application metrics.
- Configure metrics for monitoring with Prometheus.

### Task 7 – Testing (Optional)
- Configure an in-memory H2 database for testing.
- Write repository and service unit tests.
- Test REST endpoints using MockMvc.
- Implement integration tests to verify application behavior.

## Technologies

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- OAuth2 and JWT
- REST API
- H2 / PostgreSQL
- Flyway or Liquibase
- Spring Boot Actuator
- Prometheus
- JUnit and Mockito
- MockMvc
- Maven
- Git

## Progress

The tasks below are implemented in this repository.

| Task | Status |
|---|---|
| Task 1 – Hello World | Complete |
| Task 2 – CRUD REST Application | Complete |
| Task 3 – Security | Complete |
| Task 4 – Externalized Configuration | Complete |
| Task 5 – Database Migration | Complete |
| Task 6 – Actuator | Complete |
| Task 7 – Testing | Complete |

## Run locally

The default `local` profile uses a file-based H2 database. Java 21 is required.

```shell
./mvnw spring-boot:run
```

On Windows, use `.\mvnw.cmd spring-boot:run`. At startup, the application prints
`hello world` and applies pending Flyway migrations.

## REST API

Register a user (new accounts receive the `USER` role), then log in to receive a
JWT:

```shell
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"a-secure-password"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"a-secure-password"}'
```

Use the returned `accessToken` as a bearer token for `/api/tasks`. Tasks are owned
by their authenticated creator; users can only list, read, update, and delete
their own tasks. The response includes the task's `ownerUsername`. The collection
supports `GET` and `POST` at `/api/tasks`, and `GET`, `PUT`, and `DELETE` at
`/api/tasks/{id}`. Task titles are required and limited to 200 characters;
descriptions are limited to 2000 characters. Requests to `/api/admin/**`
require the `ADMIN` role; `/api/admin/status` is an example endpoint. User
registration never grants admin privileges. Grant that role only through
trusted database administration, for example:

The V2 migration adds a nullable owner reference to preserve existing task rows;
legacy tasks without an owner are not visible through user-scoped task endpoints
until an owner is assigned.

```sql
UPDATE app_users SET role = 'ADMIN' WHERE username = 'alice';
```

## Profiles and monitoring

`application-local.yml` is the default profile and uses H2. Activate `dev`,
`stg`, or `prod` with `--spring.profiles.active=<profile>`; those profiles use
PostgreSQL and require `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and
`JWT_SECRET_TOKEN` environment variables. The JWT key must be Base64-encoded
and contain at least 256 bits. Replace the local development key before
deploying.

Actuator health is available at `/actuator/health`. Other exposed actuator
endpoints require authentication. The `tasks.created` metric is available at
`/actuator/metrics/tasks.created` and `/actuator/prometheus`.
Errors use a consistent JSON response containing `status`, `code`, `message`,
and `fieldErrors`; unexpected server failures return a generic message without
exposing internal exception details.

Run the integration tests, including JWT and role authorization plus CRUD
endpoint coverage, with `./mvnw test` (Windows: `.\mvnw.cmd test`).

## Goal

The goal of this project is to gain practical experience with Spring Boot, REST API development, security, database management, monitoring, and automated testing.
