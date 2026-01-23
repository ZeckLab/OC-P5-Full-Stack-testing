# 🧘‍♀️ Yoga App — Full Test Suite - Student Project (OpenClassrooms)

![Angular](https://img.shields.io/badge/Angular-14-red)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![Jest](https://img.shields.io/badge/Jest-Tests(U&I)-blue)
![JUnit](https://img.shields.io/badge/JUnit-Tests(U&I)-blue)
![Cypress](https://img.shields.io/badge/Cypress-Tests(E2E)-darkblue)
![Status](https://img.shields.io/badge/Status-Completed-success)

This repository contains the complete test suite for the Yoga App, a booking application for a yoga studio. The Angular frontend and Spring Boot backend were already implemented by OpenClassrooms, and my role was to add all the missing tests to ensure the application could be validated before release.

The testing work included front-end tests (Jest), back-end tests (JUnit), and end-to-end tests (Cypress). The objective was to reach at least 80% code coverage, including a minimum of 30% integration tests, following the testing plan provided in the project instructions.

Some non‑critical files were intentionally left untested to respect project deadlines and focus on the most impactful areas, while still meeting the required coverage targets.

To achieve this, I implemented:
- **Jest unit & integration tests for the Angular frontend**
- **JUnit unit & integration tests for the Spring Boot backend**
- **Cypress end‑to‑end tests covering the full user journey**
- **A stable testing environment using fixtures, mocks, intercepts, and custom commands**
- **A structured approach: AAA for backend and E2E tests, and a Given/When/Then style for Angular tests**

---

# 📦 Project Structure
```text
/front     → Angular app (provided by OC) + Jest tests + Cypress tests
/back      → Spring Boot API (provided by OC) + JUnit tests
```

---

# 🔧 Prerequisites

### Backend
- Java 11+ (the project is configured for Java 8, but both versions work)
- Maven 3+
- MySQL 8 (default port 3306)

Note: Although the OC instructions mention Java 11, the backend project is configured for Java 8 (as defined in the pom.xml). Both Java 8 and Java 11 are compatible with Spring Boot 2.6.1, so either version works.

### Frontend
- Node.js 16+
- Angular CLI 14

---

# 🚀 Installation

Follow these steps to install and run the project locally:

## 1. Clone the repository

```bash
git clone https://github.com/ZeckLab/OC-P5-Full-Stack-testing.git
cd OC-P5-Full-Stack-testing
```


## 2. 🗄️ Database Setup (required before running backend)

The backend provided by OpenClassrooms requires a MySQL database to run properly and to execute integration tests.

### Create the database with MySql Command Line Client

The database name and user/password are defined in the application.properties file (under main/resources). You can modify them if needed.

```sql
CREATE DATABASE db_name CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Create a dedicated user

```sql
CREATE USER 'your_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON USE db_name.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;

USE db_name;
```

Replace 'your_user' and 'your_password' with your own credentials

### Populate the database

```sql
mysql -u your_user -p db_name < full_path/OC-P5-Full-Stack-testing/ressources/sql/script.sql
```

### Default credentials (provided by OC)

- email: yoga@studio.com
- password: test!1234

### Start MySQL locally before running the backend

The backend expects:

- host: localhost
- port: 3306
- database: test (you can change in the application.properties)

If the database is missing, the backend will fail to start and integration tests will not run.

---

## 3. 🧩 Backend Setup (Spring Boot)

The backend code was provided by OpenClassrooms.
I added **unit and integration tests** using **JUnit + Spring Boot Test**.

### Install dependencies

```bash
cd back
mvn clean install
```

### Run the backend
```bash
mvn spring-boot:run
```

Backend runs on:
```text
http://localhost:8080
```

---

## 4. 🎨 Frontend Setup (Angular)

The frontend code was provided by OpenClassrooms.
I added **unit tests (Jest)** and **E2E tests (Cypress)**.

### Install dependencies

```bash
cd front
npm install
```

### Run the frontend

```bash
npm start
```

Frontend runs on:

```text
http://localhost:4200
```

---

# 🧪 Testing Strategy

This project includes **three layers of tests**, all developed by me.

---

## ✔ 1. Angular — Jest (Unit & Integration)

Covers:
- Services
- Components
- Forms
- UI logic

### Run tests

```bash
cd front
npm run test
```

### Watch mode

```bash
npm run test:watch
```

### Generate jest coverage

```bash
cd front
npm run test --coverage
```

The main report is available at:
```text
front/coverage/lcov-report/index.html
```

---

## ✔ 2. Spring Boot — JUnit (Unit & Integration)

Covers:
- Services
- Controllers
- Security layer
- JWT utilities
- API integration tests

### Run backend tests

```bash
cd back
mvn test
```

### Generate JaCoCo coverage

```bash
mvn clean test
```

Coverage report:

```text
back/target/site/jacoco/index.html
```

---

## ✔ 3. Cypress — E2E Tests

Covers the entire user journey:

### Authentication
- Login
- Logout
- Register

### Profile
- /me page : Admin vs non-admin behavior

### Sessions lifecycle
- List
- Detail & Participate / Unparticipate
- Create
- Update
- Delete

### Run Cypress tests

```bash
cd front
npm run e2e
```

### Generate Cypress coverage
```bash
npm run e2e:coverage
```
The test results appear directly in the console.

---

# 🧰 Cypress Utilities

### ✔ Custom command: cy.login()
Used to authenticate deterministically without UI interaction.

### ✔ Fixtures
- `sessions.json` → stabilizes backend responses for all list/detail/update/delete flows
- `session-after-create.json` → represents the backend state after creating a new session
- `sessions-after-update.json` → represents the backend state after updating an existing session
- used across List, Detail, Create, Update, Delete tests

### ✔ AAA Structure
All complex tests follow:

- **Arrange** → intercepts, login, navigation
- **Act** → user actions
- **Assert** → UI expectations

### ✔ Stable intercept strategy
cy.wait() is always placed **right after the action that triggers the request**, ensuring deterministic tests.

---

# 🧭 Test Scope & Conventions

## 📝 Untested Angular Files

Some Angular services and components were not tested directly.
They mainly act as simple HTTP wrappers or orchestration layers, and their behavior is already exercised through the Angular integration tests that use them.

Since the project requirement was 80% coverage (not 100%), these files were not added to the unit test scope.

## 🚫 Backend Test Exclusions

Some backend classes were intentionally excluded from the test scope.
This follows the project instructions, which specified that certain elements (such as DTOs) were not relevant for testing and should not be included in the coverage calculation.

The following packages were excluded through the JaCoCo configuration in the pom.xml:

- models
- dto
- payload
- mapper

These folders contain data structures, mapping utilities, or boilerplate code that do not include business logic.
Excluding them ensures that the coverage percentage reflects only the meaningful parts of the backend (services, controllers, security, repositories, etc.).

All other backend components were tested to meet the required coverage targets.

## 🧩 Test File Naming Convention

To clearly distinguish the different types of tests in the project, I applied the following naming rules:

### Front (Jest)
- `*.unit.ts` → unit tests
- `*.inte.ts` → integration tests
- `*.spec.ts` → files originally provided by OpenClassrooms. These were not modified

### Back (JUnit + Spring Boot Test)
- `ClassNameTest.java` → unit tests
- `ClassNameIntTest.java` → integration tests using Spring Boot Test (MockMvc, full context)

This convention helps separate test scopes and makes the test suite easier to navigate and maintain.


# 🏁 Conclusion

This repository provides the full test suite required to validate the Yoga App before release.
It includes unit, integration, and end‑to‑end tests covering the main functional and technical areas of the application.

