# Xorcery Todo Example

A full-featured todo list application demonstrating authentication, authorization, domain-driven design, event sourcing, and Neo4j projections with Xorcery. This example shows how to build a secure, event-sourced web application with a modern architecture.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Domain Model](#domain-model)
- [Authentication & Authorization](#authentication--authorization)
- [Web UI](#web-ui)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Overview

The Todo example is a comprehensive web application demonstrating:

- **User Management** - Signup, login, profile management
- **JWT Authentication** - Token-based security
- **Role-Based Access Control** - User authorization
- **Todo Management** - CRUD operations for todo items
- **Event Sourcing** - Complete audit trail
- **Neo4j Projections** - Graph-based read models
- **Web UI** - Thymeleaf-based responsive interface
- **REST API** - JSON:API compliant endpoints

This example is ideal for understanding how to build **secure, production-ready web applications** with Xorcery.

## Features

### User Features

- ✅ **User signup** - Register new accounts
- ✅ **User login** - JWT-based authentication
- ✅ **Profile management** - View and update user profiles
- ✅ **Todo lists** - Create, read, update, delete todos
- ✅ **Todo completion** - Mark todos as done/undone
- ✅ **Filtering** - View all, active, or completed todos
- ✅ **Real-time updates** - WebSocket notifications

### Technical Features

- ✅ **JWT Authentication** - Secure token-based auth
- ✅ **Role-Based Access Control** - User/Admin roles
- ✅ **Event Sourcing** - All state changes as events
- ✅ **Domain-Driven Design** - Bounded contexts, aggregates
- ✅ **Neo4j Projections** - Efficient graph queries
- ✅ **Thymeleaf Templates** - Server-side rendering
- ✅ **JSON:API** - Standard REST API
- ✅ **OpenTelemetry** - Observability and tracing
- ✅ **DNS Service Discovery** - Service registration
- ✅ **SSL/TLS** - Secure HTTPS

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Web Browser                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Signup Page  │  │ Login Page   │  │ Todo List    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────┬───────────────────────────────────────┘
│ HTTPS/WSS
▼
┌─────────────────────────────────────────────────────────────┐
│                  Jetty Server (8443)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │SignupResource│  │AccountResource│ │TodoResource  │     │
│  │(REST)        │  │(REST)         │ │(REST)        │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                  │                  │             │
│         └──────────────────┴──────────────────┘             │
│                            │                                 │
│              ┌─────────────▼──────────────┐                │
│              │ AuthenticationRequired     │                │
│              │ Filter (JWT Validation)    │                │
│              └─────────────┬──────────────┘                │
└────────────────────────────┼─────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│                  Todo Application                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Command Handlers                         │  │
│  │  • SignupUser      • LoginUser     • CreateTodo      │  │
│  │  • UpdateAccount   • UpdateTodo    • CompleteTodo    │  │
│  │  • DeleteTodo      • ListTodos                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                      │                                       │
│                      ▼ Domain Events                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Domain Event Publisher                      │  │
│  │  • UserRegistered  • UserLoggedIn  • TodoCreated     │  │
│  │  • TodoUpdated     • TodoCompleted • TodoDeleted     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────────┘
│
┌───────────┼───────────┐
▼           ▼           ▼
┌──────────┐ ┌────────┐ ┌─────────┐
│EventStore│ │ Neo4j  │ │  JWT    │
│ (Events) │ │(Reads) │ │ Issuer  │
└──────────┘ └────────┘ └─────────┘
```

### Security Flow

```
1. User submits signup form
   ↓
2. SignupResource validates input
   ↓
3. TodoApplication.handleSignupUser()
   ↓
4. UserRegistered event published
   ↓
5. Neo4j projection creates User node
   ↓
6. User redirected to login

7. User submits login form
   ↓
8. AccountResource validates credentials
   ↓
9. JWT token generated
   ↓
10. Token returned to client
    ↓
11. Client includes token in subsequent requests
    ↓
12. AuthenticationRequiredFilter validates token
    ↓
13. Request proceeds if valid
```

## Prerequisites

### Required

- **Java 21+**
- **Maven 3.8+**

### Optional

- **Neo4j** (embedded by default, or external)
- **EventStore** (optional for event persistence)

## Quick Start

### 1. Build the Application

```bash
cd xorcery-examples-todo
mvn clean install
```

### 2. Run the Application

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.todo.Main"

# Or using JAR
java -jar target/xorcery-examples-todo-*.jar
```

### 3. Access the Application

Open your browser and navigate to:

```
https://localhost:8443/
```

**Note:** Accept the self-signed SSL certificate warning.

### 4. Create an Account

1. Click "Sign Up"
2. Enter username, email, and password
3. Click "Create Account"
4. You'll be redirected to the login page

### 5. Login

1. Enter your username and password
2. Click "Login"
3. You'll see your todo list

### 6. Manage Todos

- **Add Todo:** Type in the input box and press Enter
- **Complete Todo:** Click the checkbox
- **Delete Todo:** Click the delete button
- **Filter:** Use All/Active/Completed tabs

## Configuration

### Main Configuration

**File:** `src/main/resources/xorcery.yaml`

```yaml
# Application
application:
  name: "todo"

# Instance
instance:
  home: "{{ SYSTEM.jpackage_app-path ? jpackage.app | SYSTEM.user_dir}}"
  domain: local

# Jetty Server
jetty:
  server:
    http:
      port: 8080
    ssl:
      port: 8443

# Certificates
certificates:
  dnsNames:
    - localhost
    - "{{ instance.host }}"
  ipAddresses:
    - 127.0.0.1
    - "{{ instance.ip }}"

# REST API Resources
jersey.server.register:
  - com.exoreaction.xorcery.examples.todo.resources.AuthenticationRequiredFilter
  - com.exoreaction.xorcery.examples.todo.resources.api.StaticContentResource
  - com.exoreaction.xorcery.examples.todo.resources.api.SignupResource
  - com.exoreaction.xorcery.examples.todo.resources.api.AccountResource

# DNS Client
dns.client.search:
  - local
dns.client.hosts:
  _certificates._sub._https._tcp: "https://127.0.0.1"
dns.client.nameServers:
  - 127.0.0.1:8853

# JWT Security
jetty.server.security.jwt:
  issuers:
    server.xorcery.test:
      keys:
        - kid: "2d3f1d1f-4038-4c01-beb7-97b260462ada"
          alg: "ES256"
          publicKey: "secret:MFkwEw..."

# JWT Server (for issuing tokens)
jwt.server.keys:
  - kid: "2d3f1d1f-4038-4c01-beb7-97b260462ada"
    alg: "ES256"
    publicKey: "secret:MFkwEw..."
    privateKey: "secret:MEECAQAw..."

# DNS Server
dns.server.port: 8853

# Neo4j
neo4j:
  enabled: true
  embedded: true
  home: "{{ instance.home }}/neo4j"

# Logging
log4j2:
  Configuration:
    status: warn
    Loggers:
      Root:
        level: info
      Logger:
        - name: com.exoreaction.xorcery.examples.todo
          level: debug
```

### Environment-Specific Configuration

**Production configuration** (`xorcery-production.yaml`):

```yaml
jetty.server:
  ssl:
    port: 443

neo4j:
  embedded: false
  uri: "neo4j://neo4j-prod:7687"
  username: "neo4j"
  password: "{{ SECRETS.neo4j.password }}"

jwt.server.keys:
  - kid: "prod-key-id"
    alg: "ES256"
    publicKey: "{{ SECRETS.jwt.publicKey }}"
    privateKey: "{{ SECRETS.jwt.privateKey }}"

log4j2:
  Configuration:
    Loggers:
      Root:
        level: warn
```

## API Documentation

### Base URL

```
https://localhost:8443/api/
```

### Authentication

Most endpoints require a JWT token in the Authorization header:

```http
Authorization: Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Endpoints

#### Signup

```http
POST /api/signup
Content-Type: application/x-www-form-urlencoded

username=john&email=john@example.com&password=secret123
```

**Response:**
```http
302 Redirect to /login
```

#### Login

```http
POST /api/account/login
Content-Type: application/x-www-form-urlencoded

username=john&password=secret123
```

**Response:**
```json
{
  "token": "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

#### Get Account

```http
GET /api/account
Authorization: Bearer {token}
```

**Response:**
```json
{
  "data": {
    "type": "accounts",
    "id": "user-123",
    "attributes": {
      "username": "john",
      "email": "john@example.com",
      "createdAt": "2025-01-08T10:00:00Z"
    }
  }
}
```

#### List Todos

```http
GET /api/todos
Authorization: Bearer {token}
```

**Query parameters:**
- `filter[status]` - "active", "completed", or "all"
- `page[offset]` - Pagination offset
- `page[limit]` - Items per page

**Response:**
```json
{
  "data": [
    {
      "type": "todos",
      "id": "todo-123",
      "attributes": {
        "title": "Buy groceries",
        "completed": false,
        "createdAt": "2025-01-08T10:00:00Z"
      }
    }
  ],
  "links": {
    "self": "/api/todos",
    "next": "/api/todos?page[offset]=10"
  }
}
```

#### Create Todo

```http
POST /api/todos
Authorization: Bearer {token}
Content-Type: application/vnd.api+json

{
  "data": {
    "type": "todos",
    "attributes": {
      "title": "Learn Xorcery"
    }
  }
}
```

#### Update Todo

```http
PATCH /api/todos/{todoId}
Authorization: Bearer {token}
Content-Type: application/vnd.api+json

{
  "data": {
    "type": "todos",
    "id": "todo-123",
    "attributes": {
      "title": "Learn Xorcery framework",
      "completed": true
    }
  }
}
```

#### Delete Todo

```http
DELETE /api/todos/{todoId}
Authorization: Bearer {token}
```

## Domain Model

### Aggregates

#### User Aggregate

```java
public class User {
    private String id;
    private String username;
    private String email;
    private String passwordHash;
    private List<String> roles;
    private LocalDateTime registeredAt;
}
```

#### Todo Aggregate

```java
public class Todo {
    private String id;
    private String userId;
    private String title;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Domain Events

#### User Events
- `UserRegistered` - New user account created
- `UserLoggedIn` - User authenticated
- `ProfileUpdated` - User profile modified

#### Todo Events
- `TodoCreated` - New todo item added
- `TodoUpdated` - Todo title changed
- `TodoCompleted` - Todo marked as done
- `TodoReopened` - Completed todo marked as active
- `TodoDeleted` - Todo removed

### Neo4j Graph Model

```
(User {id, username, email})
(Todo {id, title, completed, createdAt})

(User)-[:OWNS]->(Todo)
(Todo)-[:BELONGS_TO]->(User)
```

**Cypher queries:**

```cypher
// Get user's todos
MATCH (u:User {id: $userId})-[:OWNS]->(t:Todo)
RETURN t

// Get active todos
MATCH (u:User {id: $userId})-[:OWNS]->(t:Todo {completed: false})
RETURN t

// Count completed todos
MATCH (u:User {id: $userId})-[:OWNS]->(t:Todo {completed: true})
RETURN count(t) as completedCount
```

## Authentication & Authorization

### JWT Token Structure

```json
{
  "sub": "user-123",
  "username": "john",
  "email": "john@example.com",
  "roles": ["user"],
  "iss": "server.xorcery.test",
  "iat": 1704708000,
  "exp": 1704711600
}
```

### Authentication Filter

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationRequiredFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestContext.getUriInfo().getPath())) {
            return;
        }
        
        // Extract and validate JWT token
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED).build()
            );
            return;
        }
        
        String token = authHeader.substring(7);
        // Validate token and extract user info
        // Set SecurityContext with user principal
    }
}
```

### Securing Endpoints

Use the `@RequiresAuthentication` annotation:

```java
@Path("/api/todos")
public class TodoResource {
    
    @GET
    @RequiresAuthentication
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodos(@Context SecurityContext securityContext) {
        String userId = securityContext.getUserPrincipal().getName();
        // Return user's todos
    }
}
```

## Web UI

### Pages

#### Signup Page (`/signup`)

- Username input
- Email input
- Password input
- Create Account button
- Link to Login page

#### Login Page (`/login`)

- Username input
- Password input
- Login button
- Link to Signup page

#### Account Page (`/account`)

- Display username
- Display email
- Edit profile button
- Logout button
- Link to Todo list

#### Todo List Page (`/todos`)

- Add new todo input
- Filter tabs (All / Active / Completed)
- Todo list with checkboxes
- Delete buttons
- Item count
- Clear completed button

### Thymeleaf Templates

Templates are in `src/main/resources/WEB-INF/templates/`:

- `layoutFile.html` - Base layout with header/footer
- `signup.html` - Signup form
- `account.html` - Account management
- `todos.html