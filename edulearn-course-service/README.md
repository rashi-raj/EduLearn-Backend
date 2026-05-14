# EduLearn Course Service

A Spring Boot microservice responsible for managing courses in the EduLearn platform.
It exposes REST APIs for creating, updating, publishing, and fetching courses, and
publishes domain events to Apache Kafka consumed by downstream services.

---

## Technology Stack

| Layer              | Technology                                  |
|--------------------|---------------------------------------------|
| Language           | Java 17                                     |
| Framework          | Spring Boot 3.2.5                           |
| Persistence        | Spring Data JPA + MySQL                     |
| Messaging          | Apache Kafka (spring-kafka)                 |
| Service Discovery  | Spring Cloud Netflix Eureka Client          |
| API Documentation  | SpringDoc OpenAPI (Swagger UI)              |
| Code Coverage      | JaCoCo 0.8.11                               |
| Static Analysis    | SonarQube (via sonar-maven-plugin 3.11)     |
| Build Tool         | Apache Maven                                |

---

## Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8 (database must exist and be configured in `application.yml`)
- Apache Kafka (running locally on the default port)
- Spring Cloud Eureka Service Registry running

---

## Running the Service

```bash
# Compile and run unit tests only
mvn clean test

# Compile, run tests, and generate JaCoCo coverage report
mvn clean verify

# Start the service
mvn spring-boot:run
```

---

## SonarQube Analysis

SonarQube is running locally at **http://localhost:9000** inside a Docker container.

### One-time setup (do once per project)

1. Open **http://localhost:9000** in your browser.
2. Log in (default credentials: `admin` / `admin`, then change the password).
3. Click **"Create Project"** → **Manually**.
4. Set **Project key** to `edulearn-course-service` and **Display name** to `edulearn-course-service`.
5. Choose **"Locally"** as the analysis method.
6. Generate a **project token** and copy it — you will pass it via `-Dsonar.token` on the command line.

> ⚠️ **Never paste your token into `pom.xml` or any committed file.**  
> Always supply it as a command-line argument.

---

### Running the analysis

#### Windows CMD

```cmd
mvn clean verify sonar:sonar ^
  -Dsonar.projectKey=edulearn-course-service ^
  -Dsonar.projectName=edulearn-course-service ^
  -Dsonar.host.url=http://localhost:9000 ^
  -Dsonar.token=YOUR_TOKEN
```

#### Windows PowerShell

```powershell
mvn clean verify sonar:sonar `
  -Dsonar.projectKey=edulearn-course-service `
  -Dsonar.projectName=edulearn-course-service `
  -Dsonar.host.url=http://localhost:9000 `
  -Dsonar.token=YOUR_TOKEN
```

> Replace `YOUR_TOKEN` with the token you generated in step 6 above.

---

### Verifying JaCoCo coverage report locally

After `mvn clean verify` completes, open the HTML report in your browser:

```
target/site/jacoco/index.html
```

It shows per-class, per-method, and per-line coverage metrics.

---

### Verifying the SonarQube dashboard

1. Open **http://localhost:9000** in your browser.
2. Navigate to **Projects** → **edulearn-course-service**.
3. Review the **Overview** tab for:
   - Code coverage percentage (sourced from `target/site/jacoco/jacoco.xml`)
   - Bugs, Vulnerabilities, and Code Smells
   - Security Hotspots
   - Duplications

---

### Sonar exclusions configured

The following packages are excluded from SonarQube analysis and JaCoCo coverage
because they contain generated / boilerplate code that does not require coverage:

| Package / Pattern        | Reason                              |
|--------------------------|-------------------------------------|
| `**/dto/**`              | Plain data-transfer objects (POJOs) |
| `**/config/**`           | Spring configuration classes        |
| `**/entity/**`           | JPA entity data-holders             |
| `**/event/**`            | Kafka event POJOs                   |
| `**/exception/**`        | Custom exception classes            |
| `**/*Application.java`   | Spring Boot entry-point             |

---

## API Documentation (Swagger UI)

Once the service is running, visit:

```
http://localhost:<PORT>/swagger-ui/index.html
```

---

## Project Structure

```
src/
├── main/java/com/edulearn/course/
│   ├── config/          # Spring configuration (Swagger, Kafka, etc.)
│   ├── controller/      # REST controllers
│   ├── dto/             # Request / Response DTOs
│   ├── entity/          # JPA entities
│   ├── event/           # Kafka event models
│   ├── exception/       # Custom exception classes
│   ├── repository/      # Spring Data JPA repositories
│   └── service/         # Business logic (interfaces + implementations)
└── test/java/           # Unit and integration tests
```
