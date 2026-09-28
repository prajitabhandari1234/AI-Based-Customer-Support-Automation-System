# AI-Based Customer Support Automation System

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![React](https://img.shields.io/badge/React-19.2.8-blue)
![Vite](https://img.shields.io/badge/Vite-8.1.5-purple)
![MySQL](https://img.shields.io/badge/MySQL-8.4-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)
![Azure](https://img.shields.io/badge/Azure-Container%20Apps-0078D4)
![Course](https://img.shields.io/badge/CQUniversity-COIT13230-red)

An AI-Based Customer Support Automation System that combines artificial intelligence, automated ticket management, sentiment analysis, smart escalation, knowledge-base support, role-based access control and tickets analytics in a single application.

The system supports customers, support agents and administrators through separate role-based workflows and can run locally using normal development tools, through Docker Compose, or as containerised services in a cloud environment such as Microsoft Azure.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Academic Context](#academic-context)
- [Live Application](#live-application)
- [Contributors](#contributors)
- [Main Features](#main-features)
- [User Roles](#user-roles)
- [AI Support Features](#ai-support-features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Project Structure](#project-structure)
- [Application Data](#application-data)
- [Ticket Statuses](#ticket-statuses)
- [Conversation Statuses](#conversation-statuses)
- [Message Sender Types](#message-sender-types)
- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Running Locally Without Docker](#running-locally-without-docker)
- [Running With Docker](#running-with-docker)
- [Backend Commands](#backend-commands)
- [Frontend Commands](#frontend-commands)
- [Testing](#testing)
- [Backend Testing](#backend-testing)
- [Backend API End-to-End Testing](#backend-api-end-to-end-testing)
- [Frontend Testing](#frontend-testing)
- [Frontend Browser End-to-End Testing](#frontend-browser-end-to-end-testing)
- [Frontend Performance Test](#frontend-performance-test)
- [Backend Performance Testing](#backend-performance-testing)
- [Docker Commands](#docker-commands)
- [API Overview](#api-overview)
- [Demo Accounts](#demo-accounts)
- [Production Build](#production-build)
- [Azure Deployment Architecture](#azure-deployment-architecture)
- [Security](#security)
- [Health Monitoring](#health-monitoring)
- [Troubleshooting](#troubleshooting)
- [Development Workflow](#development-workflow)
- [Important Files](#important-files)
- [Development vs Production](#development-vs-production)
- [Future Improvements](#future-improvements)
- [Academic Use](#academic-use)
- [License](#license)

---

# Project Overview

The **AI-Based Customer Support Automation System** is designed to improve the customer-support process by combining automated AI assistance with traditional human support workflows.

Customers can communicate with an AI assistant, receive immediate support responses, create support tickets and track their requests. The system analyses customer messages to determine ticket category, priority and sentiment and can escalate a request when human assistance is required.

Support agents can manage assigned and escalated tickets, communicate with customers, update ticket statuses and work with the application's knowledge base.

Administrators have additional access to user management, analytics, reports, knowledge-base management and system logs.

The application supports both a local rule-based AI mode and OpenAI integration. This allows the system to continue providing basic support functionality if the external AI service is unavailable.

---

# Academic Context

| Item | Details |
|---|---|
| Institution | **CQUniversity Australia** |
| Course Code | **COIT13230** |
| Course | **Application Development Project** |
| Project | **AI-Based Customer Support Automation System** |
| Project Type | Group Application Development Project |

---

# Live Application

> **Deployment status:** Live

### Application

```text
https://app.aics-050.workers.dev/login
```

### Health Check

```text
https://app.aics-050.workers.dev/actuator/health
```

---

# Contributors

| Contributor | Main Responsibility |
|---|---|
| **Prajita Bhandari** | Backend Development & Database |
| **Akrishta Ale** | Frontend Development & UI/UX |
| **Ayush Bhandari** | AI Integration & Research |

---

# Main Features

## Customer Features

Customers can:

- Register a new account.
- Log in securely.
- Access a personalised customer dashboard.
- Communicate with the AI customer-support assistant.
- Receive AI-generated or locally generated support responses.
- Receive automatic analysis of support issues.
- Create support tickets manually.
- Have tickets generated through the AI support workflow.
- View their support tickets.
- Open individual ticket details.
- Review ticket conversation history.
- Send messages relating to support tickets.
- View ticket status and priority.
- Receive notifications.
- Mark notifications as read.
- View unread notification indicators.
- Track whether an issue has been escalated to a human support agent.
- Access a consistent responsive web interface.

---

## Support Agent Features

Support agents can:

- Log in through a protected staff account.
- View support tickets requiring staff attention.
- View escalated tickets.
- View assigned tickets.
- Assign and manage tickets.
- Update ticket status.
- Place tickets on hold.
- Resolve support tickets.
- Add resolution information.
- Respond to customers.
- Review complete ticket histories.
- Receive staff notifications.
- Mark notifications as read.
- Access and maintain knowledge-base information.
- Use knowledge-base information while resolving customer issues.

---

## Administrator Features

Administrators can:

- Access staff functionality.
- View system-wide analytics.
- View ticket statistics.
- Filter analytics information.
- Generate weekly analytics reports.
- Generate monthly analytics reports.
- Manage application users.
- Create users.
- Update users.
- Activate or deactivate accounts.
- Delete users where permitted.
- Manage knowledge-base entries.
- Review system activity logs.
- Monitor application activity.
- Review operational ticket information.

---

# User Roles


| Role | Purpose |
|---|---|
| `CLIENT` | Customer using AI support and ticket services |
| `AGENT` | Human customer-support staff |
| `ADMIN` | Administrative and system-management access |

---

# AI Support Features

The AI functionality is one of the main components of the project.

The support system can analyse customer messages and determine:

### Ticket Category

Supported categories include:

```text
BILLING
TECHNICAL
ACCOUNT
GENERAL_INQUIRY
REFUND
ORDER_STATUS
PRODUCT_INFORMATION
```

### Priority

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### Sentiment

```text
POSITIVE
NEUTRAL
NEGATIVE
```

### AI Workflow

A customer message can be processed to identify:

- Whether the request is within the scope of customer support.
- Relevant ticket category.
- Appropriate ticket priority.
- Customer sentiment.
- Knowledge-base relevance.
- Whether human escalation is required.
- Reasons for escalation.
- A suitable customer-support response.

The application supports two AI modes.

### Local AI Mode

```text
AI_PROVIDER=local
```

This uses the application's internal support rules and does not require an external API key.

This mode is useful for:

- Local development.
- Testing.
- Automated test execution.
- Demonstrations without API usage.
- Fallback behaviour.

### OpenAI Mode

```text
AI_PROVIDER=openai
```

This enables OpenAI integration through the Responses API.

Configuration:

```text
OPENAI_ENDPOINT=https://api.openai.com/v1/responses
OPENAI_MODEL=gpt-5.6-luna
OPENAI_API_KEY=<YOUR_API_KEY>
```

If the OpenAI request fails, the application contains fallback behaviour so that the support workflow can continue using local logic.

The assistant is intentionally limited to customer-support-related requests. Requests unrelated to customer support can be rejected without creating unnecessary support tickets.

---

# Technology Stack

## Backend

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot 4.1.1 | Application framework |
| Spring MVC | REST API |
| Spring Security | Authentication and authorisation |
| Spring Data JPA | Database persistence |
| Hibernate | ORM |
| JWT / JJWT 0.12.6 | Stateless authentication |
| BCrypt | Password hashing |
| Jakarta Validation | Request validation |
| Spring Boot Actuator | Application health monitoring |
| Lombok | Boilerplate reduction |
| Maven | Build and dependency management |
| Maven Wrapper | Consistent Maven execution |

---

## Database

| Technology | Usage |
|---|---|
| H2 | Simple local backend development and automated testing |
| MySQL 8.4 | Docker and production database |
| Azure Database for MySQL | Managed production database |

The backend automatically selects the database profile using:

```text
APP_DB_MODE=h2
```

or:

```text
APP_DB_MODE=mysql
```

---

## Frontend

| Technology | Purpose |
|---|---|
| React 19.2.8 | User interface |
| React DOM | Browser rendering |
| React Router 7.18.1 | Client-side routing |
| Vite 8.1.5 | Development and production build system |
| Chart.js 4.4.6 | Analytics visualisation |
| JavaScript ES Modules | Frontend development |
| CSS | Application styling |
| Nginx | Production static hosting and backend reverse proxy |

---

## Testing

| Technology | Purpose |
|---|---|
| JUnit / Spring Boot Test | Backend automated tests |
| Spring Security Test | Security testing |
| Vitest 4.1.10 | Frontend unit and integration testing |
| Node.js Browser Harness | Frontend browser E2E testing |
| Chrome / Chromium Headless | Browser automation |
| Bash + cURL | Backend API E2E testing |
| k6 | Backend load and performance testing |

---

## DevOps and Deployment

| Technology | Purpose |
|---|---|
| Docker | Application containerisation |
| Docker Compose | Local full-stack environment |
| Nginx | Frontend reverse proxy |
| Azure Container Registry | Production Docker image registry |
| Azure Container Apps | Frontend/backend cloud hosting |
| Azure Database for MySQL Flexible Server | Managed database |
| Azure Monitor / Container Logs | Production monitoring |

---

# System Architecture

## Local Development

```text
Browser
   |
   v
React + Vite
localhost:5173
   |
   | /api
   | /actuator
   v
Spring Boot
localhost:8080
   |
   v
H2 Database
```

---

## Local Docker Architecture

```text
Browser
   |
   v
Frontend Container
React + Nginx
localhost:8081
   |
   | http://backend:8080
   v
Backend Container
Spring Boot
localhost:8080
   |
   | mysql:3306
   v
MySQL Container
MySQL 8.4
```

---

## Production Architecture

```text
                         Internet
                            |
                            | HTTPS
                            v
               +-------------------------+
               | Frontend Container App  |
               | React + Nginx           |
               | Public port :443     |
               +------------+------------+
                            |
                            | /api
                            | /actuator
                            |
                            v
               +-------------------------+
               | Backend Container App   |
               | Spring Boot             |
               | Internal port :8080  |
               +------------+------------+
                            |
                            | TLS / MySQL
                            v
               +-------------------------+
               | Azure Database          |
               | for MySQL               |
               +-------------------------+

                       Backend
                          |
                          v
                    OpenAI API
```

---

# Project Structure

```text
AI-Customer-Support/
│
├── .env.example
├── .gitignore
├── docker-compose.yml
├── README.md
│
├── backend/
│   ├── .dockerignore
│   ├── .env.example
│   ├── .gitignore
│   ├── Dockerfile
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   │
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/cqu/coit13230/AIBasedCustomerSupport/
│       │   │       ├── config/
│       │   │       ├── controller/
│       │   │       ├── dto/
│       │   │       ├── exception/
│       │   │       ├── model/
│       │   │       ├── repository/
│       │   │       ├── security/
│       │   │       └── service/
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-h2.yml
│       │       └── application-mysql.yml
│       │
│       └── test/
│           ├── java/
│           │   └── com/cqu/coit13230/AIBasedCustomerSupport/
│           │       ├── testing/
│           │       ├── e2e/
│           │       └── performance/
│           │
│           └── resources/
│               └── application-test.yml
│
└── frontend/
   ├── .dockerignore
   ├── .env.example
   ├── .gitignore
   ├── Dockerfile
   ├── nginx.conf
   ├── package.json
   ├── package-lock.json
   ├── vite.config.js
   ├── jsdoc.json
   ├── index.html
   │
   ├── src/
   │   ├── api/
   │   ├── assets/
   │   ├── auth/
   │   ├── components/
   │   ├── config/
   │   ├── pages/
   │   ├── utils/
   │   ├── App.jsx
   │   ├── main.jsx
   │   └── styles.css
   │
   └── tests/
       ├── browser/
       ├── e2e/
       └── performance/

```

---

# Application Data

The main backend entities include:

```text
User
Ticket
Conversation
Message
Notification
KnowledgeBaseEntry
SystemLog
```

Ticket information includes attributes such as:

```text
Category
Priority
Sentiment
Status
Assigned Agent
Customer
Escalation State
Conversation
Messages
Resolution Information
Created / Updated Times
```

---

# Ticket Statuses

The supported ticket states are:

```text
OPEN
ESCALATED
IN_PROGRESS
ON_HOLD
RESOLVED_BY_AI
RESOLVED
CLOSED
```

---

# Conversation Statuses

```text
ACTIVE
COMPLETED
ESCALATED
```

---

# Message Sender Types

```text
CLIENT
AI
AGENT
SYSTEM
```

---

# Prerequisites

The following software is required depending on how the application is run.

## Required for Standard Local Development

### Backend

- Java **21**
- Internet connection for initial Maven dependency download

Maven does **not** need to be installed separately because the project includes:

```text
backend/mvnw
backend/mvnw.cmd
```

### Frontend

- Node.js **20.19 or newer**
- npm

Check:

```bash
node --version
npm --version
```

### Docker Development

Install:

- Docker Desktop

Check:

```bash
docker --version
docker compose version
```

### Optional

- MySQL 8.4 for direct MySQL development
- Google Chrome or Chromium for frontend browser E2E tests
- k6 for backend performance testing
- OpenAI API key when using `AI_PROVIDER=openai`

---

# Environment Configuration

The project has separate environment configuration according to how the application is started.

## Environment Files

| File | Purpose |
|---|---|
| `backend/.env` | Backend running directly from terminal |
| `frontend/.env` | Frontend running directly from terminal |
| `.env` | Full application running through Docker Compose |
| `backend/.env.example` | Safe backend configuration template |
| `frontend/.env.example` | Safe frontend configuration template |
| `.env.example` | Safe Docker Compose template |

---

## Create Local Environment Files

From the project root:

```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
cp .env.example .env
```

Windows users can copy the files manually or use:

```cmd
copy backend\.env.example backend\.env
copy frontend\.env.example frontend\.env
copy .env.example .env
```

---


# Running Locally Without Docker

This is the development mode because the backend can use H2 without requiring MySQL.

---

## Terminal 1 — Backend

```bash
cd backend
./mvnw spring-boot:run
```

Windows:

```cmd
cd backend
mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

The default local configuration uses:

```text
APP_DB_MODE=h2
```

so a separate MySQL installation is not required.

---

## Terminal 2 — Frontend

```bash
cd frontend
npm ci
npm run dev
```

Frontend:

```text
http://localhost:5173
```

Vite proxies:

```text
/api
/actuator
```

to:

```text
http://localhost:8080
```

---

# Backend Commands

Move into:

```bash
cd backend
```

## Start Backend

```bash
./mvnw spring-boot:run
```

Windows:

```cmd
mvnw.cmd spring-boot:run
```

---

## Compile

```bash
./mvnw compile
```

---

## Clean

```bash
./mvnw clean
```

---

## Package Application

```bash
./mvnw clean package
```

The generated JAR will be placed under:

```text
backend/target/
```

---

## Package Without Tests

Use only when deliberately skipping tests:

```bash
./mvnw clean package -DskipTests
```

---

## Run All Backend Automated Tests

```bash
./mvnw test
```

---

## Run a Specific Test Class

Example:

```bash
./mvnw -Dtest=JwtServiceUnitTest test
```

Another example:

```bash
./mvnw -Dtest=AuthenticationAndSecuritySystemTest test
```

---

# Frontend Commands

Move into:

```bash
cd frontend
```

---

## Install Exact Dependencies

```bash
npm ci
```


---

## Start Development Server

```bash
npm run dev
```

Application:

```text
http://localhost:5173
```

---

## Build Production Frontend

```bash
npm run build
```

Generated files:

```text
frontend/dist/
```

---

## Preview Production Build

```bash
npm run preview
```

---

## Run Frontend Unit Tests

```bash
npm test
```

or:

```bash
npm run test:unit
```

---

## Run Frontend End-to-End Test

```bash
npm run test:e2e
```

---

## Run Frontend Performance Test

```bash
npm run test:performance
```

---

## Run All Frontend Tests

```bash
npm run test:all
```

---

## Generate Frontend JSDoc

```bash
npm run docs
```

---

# Testing

The project includes multiple levels of testing rather than relying only on unit tests.

The test coverage includes:

```text
Unit Testing
Integration Testing
Security Testing
System Testing
API End-to-End Testing
Browser End-to-End Testing
Performance Smoke Testing
Load Testing
Stress Testing
Spike Testing
Soak Testing
Breakpoint Testing
Concurrent Update Testing
AI Fallback / Resilience Testing
Validation and Error Testing
```

---

# Backend Testing

Backend tests are located under:

```text
backend/src/test/
```

Important test classes include:

```text
AiBasedCustomerSupportApplicationTests
AgentAdminWorkflowSystemTest
AiDecisionIntegrationTest
ApiErrorAndCrudSystemTest
AuthenticationAndSecuritySystemTest
CustomerChatTicketSystemTest
EnumAndValidationUnitTest
JwtServiceUnitTest
OpenAiFallbackResilienceTest
```

Run all backend tests:

```bash
cd backend
./mvnw test
```

These tests cover areas including:

- Authentication.
- JWT handling.
- Authorisation.
- Role restrictions.
- User management.
- Customer workflows.
- Agent workflows.
- Admin workflows.
- Ticket creation.
- Ticket updates.
- Conversations.
- Messages.
- Notifications.
- Knowledge base.
- Analytics.
- Validation.
- Error handling.
- AI categorisation.
- AI scope handling.
- Local AI fallback.
- OpenAI failure handling.

---

# Backend API End-to-End Testing

The backend contains a separate E2E test flow using Bash and cURL.

Files:

```text
backend/src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/e2e/
├── start_test_backend.sh
└── run_e2e.sh
```

## Terminal 1

```bash
cd backend
./src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/e2e/start_test_backend.sh
```

This starts the backend using:

```text
H2
SEED_DATA=true
AI_PROVIDER=local
```

---

## Terminal 2

```bash
cd backend
./src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/e2e/run_e2e.sh
```

If execute permission is missing:

```bash
chmod +x src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/e2e/*.sh
```

The E2E flow checks real HTTP interactions including:

- Health endpoint.
- Admin login.
- Agent login.
- Customer login.
- New customer registration.
- Authentication checks.
- Role restrictions.
- Conversation creation.
- Ticket creation.
- Ticket retrieval.
- AI chat.
- Out-of-scope AI handling.
- Agent workflow.
- Knowledge-base operations.
- Analytics.
- System logs.
- Invalid login handling.
- Invalid request validation.

---

# Frontend Testing

Frontend unit and integration tests are located alongside the related frontend source modules and under:

```text
frontend/tests/
```

Run:

```bash
cd frontend
npm ci
npm run test:unit
```

Tested frontend areas include:

- API client.
- API response handling.
- Data normalisation.
- Components.
- Utility formatting.
- API integration behaviour.

---

# Frontend Browser End-to-End Testing

Run:

```bash
cd frontend
npm run test:e2e
```

The browser test automatically:

1. Builds the frontend.
2. Starts a deterministic mock backend.
3. Starts the Vite production preview.
4. Launches Chrome/Chromium in headless mode.
5. Logs in as a test customer.
6. Verifies the customer dashboard.
7. Tests the AI chat interface.
8. Tests support-ticket creation.
9. Tests ticket-detail navigation.

Chrome or Chromium must be installed.

If the browser cannot be automatically located, set:

```bash
export CHROME_BIN="/path/to/chrome"
```

Example on macOS:

```bash
export CHROME_BIN="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
```

---

# Frontend Performance Test

Run:

```bash
cd frontend
npm run test:performance
```

The frontend performance smoke test checks budgets including:

```text
DOMContentLoaded < 3000 ms
Page load < 5000 ms
Resources < 80
Transferred data < 8 MB
```

---

# Backend Performance Testing

Backend performance scripts use **k6**.

Location:

```text
backend/src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/
```

Available scenarios:

```text
smoke.js
load.js
stress.js
spike.js
soak.js
breakpoint.js
chat_load.js
concurrent_ticket_updates.js
```

---

## Install k6

macOS:

```bash
brew install k6
```

Verify:

```bash
k6 version
```

Before running performance tests, start the backend with development seed data.

Example:

```bash
cd backend
./src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/e2e/start_test_backend.sh
```

---

## Smoke Test

```bash
cd backend
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/smoke.js
```

---

## Normal Load Test

```bash
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/load.js
```

---

## Stress Test

```bash
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/stress.js
```

---

## Traffic Spike Test

```bash
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/spike.js
```

---

## Soak Test

Default duration is approximately 30 minutes:

```bash
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/soak.js
```

A custom duration can be supplied:

```bash
DURATION=10m VUS=10 k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/soak.js
```

---

## AI Chat Load Test

```bash
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/chat_load.js
```

Custom values:

```bash
VUS=20 DURATION=5m k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/chat_load.js
```

---

## Breakpoint Test

This intentionally increases load significantly.

```bash
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/breakpoint.js
```

---

## Concurrent Ticket Update Test

This requires the ID of a ticket assigned to the demonstration agent.

```bash
TICKET_ID=<TICKET_ID> \
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/concurrent_ticket_updates.js
```

Optional:

```bash
TICKET_ID=<TICKET_ID> \
VUS=20 \
ITERATIONS=100 \
k6 run src/test/java/com/cqu/coit13230/AIBasedCustomerSupport/performance/concurrent_ticket_updates.js
```

---

# Running With Docker

Docker Compose runs the complete application:

```text
Frontend
Backend
MySQL
```

First create the root environment file:

```bash
cp .env.example .env
```

Update private values such as:

```text
MYSQL_PASSWORD
MYSQL_ROOT_PASSWORD
JWT_SECRET
```

If using OpenAI:

```text
AI_PROVIDER=openai
OPENAI_API_KEY=<YOUR_API_KEY>
```

Otherwise:

```text
AI_PROVIDER=local
```

---

## Start Application

From the project root:

```bash
docker compose up --build
```

Application URLs:

| Service | Address |
|---|---|
| Frontend | `http://localhost:8081` |
| Backend | `http://localhost:8080` |
| Health | `http://localhost:8080/actuator/health` |
| MySQL | `localhost:3306` |

---

## Run Docker in Background

```bash
docker compose up --build -d
```

---

## Stop Containers

```bash
docker compose down
```

---

# Docker Commands

## View Containers

```bash
docker compose ps
```

---

## View All Logs

```bash
docker compose logs
```

---

## Backend Logs

```bash
docker compose logs backend
```

Follow logs:

```bash
docker compose logs -f backend
```

---

## Frontend Logs

```bash
docker compose logs frontend
```

---

## MySQL Logs

```bash
docker compose logs mysql
```

---

## Rebuild Everything

```bash
docker compose down
docker compose up --build
```

---

## Rebuild Only Backend

```bash
docker compose build backend
docker compose up -d backend
```

---

## Rebuild Only Frontend

```bash
docker compose build frontend
docker compose up -d frontend
```

---

## Test Backend From Frontend Container

```bash
docker compose exec frontend wget -qO- http://backend:8080/actuator/health
```

---

## Test Complete Frontend Proxy

```bash
curl http://localhost:8081/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

The response may also contain Spring Boot health groups.

---

## Completely Reset Local Docker Database

```bash
docker compose down -v
docker compose up --build
```

---

# API Overview

The backend exposes REST APIs under:

```text
/api
```

Important API areas include:

| API Area | Base Path |
|---|---|
| Authentication | `/api/auth` |
| AI Chat | `/api/chat` |
| General Tickets | `/api/tickets` |
| Customer Tickets | `/api/customer/tickets` |
| Customer Conversations | `/api/customer/conversations` |
| Customer Notifications | `/api/customer/notifications` |
| Agent Tickets | `/api/agent/tickets` |
| Agent Notifications | `/api/agent/notifications` |
| Conversations | `/api/conversations` |
| Messages | `/api/messages` |
| Notifications | `/api/notifications` |
| Users | `/api/users` |
| Admin Users | `/api/admin/users` |
| Knowledge Base | `/api/knowledge-base` |
| Analytics | `/api/analytics` and `/api/admin/analytics` |
| System Logs | `/api/system-logs` |
| Health | `/actuator/health` |

Protected endpoints require:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# Demo Accounts

When:

```text
SEED_DATA=true
```

the backend creates demonstration accounts.

> **These accounts are only for local development, testing and project demonstration.**

### Administrator

```text
Email:    admin@support.local
Password: Admin123!
Role:     ADMIN
```

### Support Agent

```text
Email:    agent@support.local
Password: Agent123!
Role:     AGENT
```

### Customer

```text
Email:    customer@support.local
Password: Customer123!
Role:     CLIENT
```

Production use case:

```text
SEED_DATA=false
```

---

# Production Build

## Backend Docker Image

From the project root:

```bash
docker build \
  -t ai-support-backend:latest \
  ./backend
```

---

## Frontend Docker Image

```bash
docker build \
  --build-arg VITE_API_BASE_URL= \
  --build-arg VITE_API_TIMEOUT_MS=30000 \
  --build-arg VITE_ENABLE_DEMO_ACCOUNTS=false \
  -t ai-support-frontend:latest \
  ./frontend
```

---

# Azure Deployment Architecture

Recommended Azure resources:

```text
Azure Resource Group
│
├── Azure Container Registry
│   ├── ai-support-backend:<version>
│   └── ai-support-frontend:<version>
│
├── Azure Container Apps Environment
│   │
│   ├── support-frontend
│   │   ├── External port
│   │   ├── HTTPS
│   │   └── Port 80
│   │
│   └── support-backend
│       ├── Internal port
│       └── Port 8080
│
└── Azure Database for MySQL Flexible Server
    └── ai_customer_support
```

The frontend communicates with the backend using the internal Azure Container Apps service name:

```text
BACKEND_API_URL=http://support-backend
```

The public user only accesses:

```text
https://app.aics-050.workers.dev
```

---

# Security

Security controls implemented by the application include:

- JWT-based authentication.
- Stateless backend security.
- BCrypt password hashing.
- Role-based access control.
- Protected frontend routes.
- Protected backend endpoints.
- User account status checking.
- Request validation.
- CORS restrictions.
- Global API error handling.
- Separation between customer, agent and administrator permissions.
- Configurable JWT expiration.
- Environment-based secret management.
- Internal backend production architecture.
- TLS-enabled managed MySQL configuration.
- Production seed-data disabling.

---

## JWT Secret

Example generation command:

```bash
openssl rand -base64 64
```

---

## OpenAI API Key


The key must only be available to the backend.

For Azure deployment, store it using Azure Container App secrets.

---

## Database Security

Local Docker development can use:

```text
MYSQL_SSL_MODE=DISABLED
```

Production should use:

```text
MYSQL_SSL_MODE=REQUIRED
```

Production credentials should be different from development credentials.

---

# Health Monitoring

Spring Boot Actuator provides application health monitoring.

Local backend:

```text
http://localhost:8080/actuator/health
```

Docker frontend proxy:

```text
http://localhost:8081/actuator/health
```

Production:

```text
https://app.aics-050.workers.dev/actuator/health
```

Expected successful status:

```json
{
  "status": "UP"
}
```

---

# Troubleshooting

## Backend Does Not Start

Check Java:

```bash
java --version
```

Java 21 should be available.

Then try:

```bash
cd backend
./mvnw clean
./mvnw spring-boot:run
```

---

## Frontend Does Not Start

Check Node:

```bash
node --version
```

The project requires:

```text
Node >= 20.19
```

Then reinstall dependencies:

```bash
cd frontend
rm -rf node_modules
npm ci
npm run dev
```

---

## Frontend Shows Backend Unavailable

Check:

```text
http://localhost:8080/actuator/health
```

For Docker:

```bash
docker compose ps
docker compose logs backend
curl http://localhost:8081/actuator/health
```

Docker frontend configuration should use:

```text
BACKEND_API_URL=http://backend:8080
```

Azure should use:

```text
BACKEND_API_URL=http://support-backend
```

---

## Docker MySQL Does Not Start

Check:

```bash
docker compose logs mysql
```

Verify that `.env` contains:

```text
MYSQL_DATABASE
MYSQL_USER
MYSQL_PASSWORD
MYSQL_ROOT_PASSWORD
```

If the development database can safely be deleted:

```bash
docker compose down -v
docker compose up --build
```

---

## OpenAI Is Not Working

Check:

```text
AI_PROVIDER=openai
```

and confirm:

```text
OPENAI_API_KEY
OPENAI_MODEL
OPENAI_ENDPOINT
```

are configured correctly.

For development without OpenAI:

```text
AI_PROVIDER=local
```

The application can therefore be tested without making external AI requests.

---

## Browser E2E Test Cannot Find Chrome

Set:

```bash
export CHROME_BIN="/path/to/chrome"
```

macOS example:

```bash
export CHROME_BIN="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
```

Then:

```bash
npm run test:e2e
```

---


# Development Workflow

A typical development workflow is:

```text
1. Pull latest source
        ↓
2. Configure local .env files
        ↓
3. Start backend
        ↓
4. Start frontend
        ↓
5. Develop / test functionality
        ↓
6. Run backend tests
        ↓
7. Run frontend tests
        ↓
8. Test full application using Docker
        ↓
9. Review logs and health checks
        ↓
10. Commit source changes
        ↓
11. Build production containers
        ↓
12. Deploy versioned containers
```

Before submitting or deploying a new version, the recommended minimum verification is:

```bash
cd backend
./mvnw test
```

then:

```bash
cd ../frontend
npm ci
npm run test:all
```

then from the project root:

```bash
docker compose down
docker compose up --build
```

and finally:

```bash
curl http://localhost:8081/actuator/health
```

---

# Important Files

| File | Purpose |
|---|---|
| `docker-compose.yml` | Complete local Docker environment |
| `backend/pom.xml` | Backend dependencies and Maven configuration |
| `backend/Dockerfile` | Backend production container |
| `backend/.env.example` | Backend environment template |
| `backend/src/main/resources/application.yml` | Main Spring configuration |
| `backend/src/main/resources/application-h2.yml` | H2 database configuration |
| `backend/src/main/resources/application-mysql.yml` | MySQL configuration |
| `frontend/package.json` | Frontend dependencies and commands |
| `frontend/Dockerfile` | Frontend production container |
| `frontend/nginx.conf` | Production web server and reverse proxy |
| `frontend/vite.config.js` | Vite development configuration |
| `frontend/.env.example` | Frontend environment template |

---

# Development vs Production

| Configuration | Development | Production |
|---|---|---|
| Database | H2 / local MySQL | Managed MySQL |
| Seed Data | `true` | `false` |
| AI | Local or OpenAI | OpenAI with fallback |
| Backend exposure | localhost | Internal/private |
| Frontend | Vite | Nginx container |
| Error messages | Detailed | Restricted |
| MySQL SSL | Optional | Required |
| Demo accounts | Allowed | Disabled |
| Secrets | Local `.env` | Platform secrets |
| HTTPS | Optional | Required |
| Docker images | Local | Versioned registry images |

---

# Future Improvements

The current application provides a complete working customer-support workflow. Possible future improvements include:

- Flyway or Liquibase database migrations.
- Automated CI/CD using GitHub Actions or Azure DevOps.
- Fully private Azure MySQL networking.
- Azure Key Vault integration.
- Custom production domain.
- Automated TLS/custom-domain management.
- Expanded application monitoring.
- Structured observability and metrics.
- Additional AI evaluation tests.
- Expanded knowledge-base search.
- Email notification integration.
- File attachments for support tickets.
- Password reset workflow.
- Multi-factor authentication.
- Additional analytics dashboards.
- Ticket SLA tracking.
- Automated backup verification.
- Horizontal backend scaling.
- Container security hardening.
- Additional accessibility testing.


---

# Academic Use

This repository was developed as part of the **COIT13230 Application Development Project at CQUniversity Australia**.

It demonstrates practical application of:

- Full-stack web development.
- Requirements implementation.
- REST API development.
- Frontend/backend integration.
- Relational database design.
- Authentication and authorisation.
- Artificial intelligence integration.
- Automated customer-support workflows.
- Software testing.
- System testing.
- End-to-end testing.
- Performance testing.
- Containerisation.
- Environment configuration.
- Cloud deployment planning.
- Security considerations.
- Collaborative software development.

The project is intended to demonstrate the complete application-development lifecycle from proposal and planning through implementation, testing and deployment.

---

# License

No redistribution or reuse rights unless a licence is added.
