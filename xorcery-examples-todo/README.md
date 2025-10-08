
# Xorcery Todo Example

A todo list application demonstrating domain-driven design, event sourcing, and Neo4j projections with Xorcery. This example shows how to build an event-sourced web application with a modern architecture.

## Table of Contents

- [Overview](#overview)
- [Current Status](#current-status)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Domain Model](#domain-model)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Next Steps](#next-steps)
- [Troubleshooting](#troubleshooting)

## Overview

The Todo example demonstrates Xorcery's core capabilities:

- **Domain-Driven Design** - Aggregates, commands, and events
- **Event Sourcing** - Complete audit trail of state changes
- **Neo4j Projections** - Graph-based read models
- **REST API** - JSON:API compliant endpoints
- **JWT Authentication** - Token-based security
- **Reactive Streams** - WebSocket-based event streaming
- **DNS Service Discovery** - Built-in service registration

This example is ideal for understanding how to build **event-sourced applications** with Xorcery.

## Current Status

✅ **Compiling and Building**
✅ **Application Starts Successfully**
✅ **Core Services Registered**
- TodoApplication service
- Domain event publishing
- Neo4j projections
- JWT server
- DNS server and client

🚧 **In Progress**
- Thymeleaf templates (HTML UI)
- Complete REST API endpoints
- End-to-end functional tests

## Features

### Technical Features Implemented

- ✅ **Event Sourcing** - Domain events for all state changes
- ✅ **Domain-Driven Design** - Entities, commands, contexts
- ✅ **Neo4j Projections** - Event-driven graph updates
- ✅ **JWT Security** - Token generation and validation
- ✅ **DNS Service Discovery** - Built-in DNS server and client
- ✅ **Reactive Streams** - WebSocket-based event streaming
- ✅ **SSL/TLS** - Automatic certificate generation
- ✅ **Embedded Neo4j** - No external database needed for development

### Domain Model

The application includes three main entities:

1. **User** - User accounts with authentication
2. **Project** - Projects containing tasks (formerly "Post")
3. **Task** - Individual todo items (formerly "Comment")

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Jetty Server (HTTPS)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │SignupResource│  │AccountResource│ │ (Future      │     │
│  │              │  │               │ │  Resources)  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
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
│  │  • SignupUser      • CreateProject    • AddTask      │  │
│  │  • UpdateProject   • UpdateTask      • RemoveTask    │  │
│  └──────────────────────────────────────────────────────┘  │
│                      │                                       │
│                      ▼ Domain Events                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Domain Event Publisher                      │  │
│  │  • UserRegistered  • TaskCreated   • TaskUpdated     │  │
│  │  • TaskRemoved     • ProjectCreated                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────────┘
│
┌───────────┼───────────┐
▼           ▼           ▼
┌──────────┐ ┌────────┐ ┌─────────┐
│ WebSocket│ │ Neo4j  │ │  JWT    │
│ Streams  │ │(Reads) │ │ Issuer  │
└──────────┘ └────────┘ └─────────┘
```

## Prerequisites

### Required

- **Java 21+**
- **Maven 3.8+**

### Optional

- **Neo4j Browser** - To inspect the graph database (http://localhost:7474)

## Quick Start

### 1. Build the Module

```bash
cd xorcery-examples-todo
mvn clean install
```

### 2. Run Tests

```bash
mvn test
```

This will:
- Start the Xorcery application
- Initialize embedded Neo4j
- Start DNS server
- Run basic smoke tests

### 3. Run the Application

```bash
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.todo.Main"
```

Or using the JAR:

```bash
java -jar target/xorcery-examples-todo-*.jar
```

### 4. Access the Application

Open your browser and navigate to:

```
https://localhost:8443/
```

**Note:** Accept the self-signed SSL certificate warning.

## Configuration

### Main Configuration

**File:** `src/main/resources/xorcery.yaml`

```yaml
# Application
application.name: "todo"

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

# REST API Resources
jersey.server.register:
  - com.exoreaction.xorcery.examples.todo.resources.AuthenticationRequiredFilter
  - com.exoreaction.xorcery.examples.todo.resources.api.StaticContentResource
  - com.exoreaction.xorcery.examples.todo.resources.api.SignupResource
  - com.exoreaction.xorcery.examples.todo.resources.api.AccountResource

# DNS Configuration
dns:
  server:
    port: 8853
  client:
    search:
      - local
    nameServers:
      - 127.0.0.1:8853

# JWT Security
jetty.server.security.jwt:
  issuers:
    server.xorcery.test:
      keys:
      - kid: "2d3f1d1f-4038-4c01-beb7-97b260462ada"
        alg: "ES256"
        publicKey: "secret:MFkwEw..."

jwt.server.keys:
  - kid: "2d3f1d1f-4038-4c01-beb7-97b260462ada"
    alg: "ES256"
    publicKey: "secret:MFkwEw..."
    privateKey: "secret:MEECAQAw..."

# Neo4j
neo4j:
  enabled: true
  embedded: true
```

### Test Configuration

Tests automatically configure:
- Random SSL port to avoid conflicts
- Disabled DNS registration
- Embedded Neo4j
- Local DNS server and client

## Domain Model

### Aggregates

#### UserEntity

```java
@Create
public record Signup(String id, String email, String password) implements Command {}
```

**Events:** `UserRegistered`

#### ProjectEntity

```java
@Create
public record CreatePost(String id, String title, String body) implements Command {}

@Update
public record UpdatePost(String id, String title, String body) implements Command {}
```

**Events:** `createdpost`, `updatedpost`

#### TaskEntity

```java
@Create
public record AddTask(String id, String projectId, String description) implements Command {}

@Update
public record UpdateTask(String id, String description) implements Command {}

@Delete
public record RemoveTask(String id) implements Command {}
```

**Events:** `TaskCreated`, `TaskUpdated`, `TaskRemoved`

### Domain Events

All events follow the JSON Domain Event format and include:
- Event type
- Entity type and ID
- Attribute updates
- Relationship changes
- Metadata (timestamp, correlation ID, etc.)

### Neo4j Graph Model

```cypher
// Nodes
(User {id, email, passwordHash, status})
(Project {id, title, body})
(Task {id, description})

// Relationships
(Project)-[:ProjectTasks]->(Task)
```

## Testing

### Run All Tests

```bash
mvn test
```

### Current Tests

1. **testApplicationStarts** - Verifies the application initializes correctly
2. **testServiceLocatorHasTodoApplication** - Checks service registration
3. **testServerIsRunning** - Confirms HTTPS server is active

### Test Structure

```java
@RegisterExtension
static XorceryExtension xorceryExtension = XorceryExtension.xorcery()
    .configuration(ConfigurationBuilder::addTestDefaults)
    .addYaml("""
        jetty.server.ssl.port: <random>
        dns.registration.enabled: false
        dns.server.enabled: true
        dns.client.enabled: true
        neo4j.enabled: true
        neo4j.embedded: true
        """)
    .build();
```

### Adding New Tests

```java
@Test
void testCustomFeature() throws Exception {
    Configuration config = xorceryExtension.getServiceLocator()
        .getService(Configuration.class);
    
    // Your test logic here
}
```

## Project Structure

```
xorcery-examples-todo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/exoreaction/xorcery/examples/todo/
│   │   │       ├── Main.java
│   │   │       ├── TodoService.java
│   │   │       ├── contexts/          # Domain contexts
│   │   │       │   ├── SignupContext.java
│   │   │       │   ├── PostContext.java
│   │   │       │   ├── PostsContext.java
│   │   │       │   ├── PostCommentsContext.java
│   │   │       │   └── CommentContext.java
│   │   │       ├── entities/          # Domain entities
│   │   │       │   ├── DomainModel.java
│   │   │       │   ├── UserEntity.java
│   │   │       │   ├── ProjectEntity.java
│   │   │       │   └── TaskEntity.java
│   │   │       ├── model/             # Read models
│   │   │       │   ├── ForumModel.java
│   │   │       │   ├── PostModel.java
│   │   │       │   ├── Posts.java
│   │   │       │   ├── CommentModel.java
│   │   │       │   └── Comments.java
│   │   │       └── resources/         # REST API
│   │   │           ├── TodoApplication.java
│   │   │           ├── AuthenticationRequiredFilter.java
│   │   │           ├── RequiresAuthentication.java
│   │   │           └── api/
│   │   │               ├── SignupResource.java
│   │   │               ├── AccountResource.java
│   │   │               ├── ThymeleafResource.java
│   │   │               └── StaticContentResource.java
│   │   └── resources/
│   │       ├── xorcery.yaml
│   │       └── WEB-INF/
│   │           └── templates/         # Thymeleaf templates (to be added)
│   │               ├── layoutFile.html
│   │               ├── signup.html
│   │               └── account.html
│   └── test/
│       └── java/
│           └── com/exoreaction/xorcery/examples/todo/
│               └── TodoServiceTest.java
├── pom.xml
└── README.md
```

## Next Steps

### Immediate TODOs

1. **Create Thymeleaf Templates**
    - signup.html - User registration form
    - account.html - User account page
    - layoutFile.html - Base layout template

2. **Implement REST Endpoints**
    - POST /todo/signup - User registration
    - GET /todo/account - User account details
    - GET /api/projects - List projects
    - POST /api/projects - Create project
    - GET /api/projects/{id}/tasks - List tasks
    - POST /api/projects/{id}/tasks - Add task

3. **Add Functional Tests**
    - User signup flow
    - Project creation
    - Task management
    - Event sourcing verification

4. **Enhance Security**
    - Password hashing (bcrypt)
    - Role-based access control
    - Token refresh mechanism

### Future Enhancements

- **EventStore Integration** - Persistent event store
- **WebSocket UI Updates** - Real-time updates via reactive streams
- **Search Functionality** - Full-text search with Neo4j
- **Multi-tenant Support** - Isolated user workspaces
- **Audit Trail UI** - Visualize event history
- **API Documentation** - OpenAPI/Swagger integration

## Troubleshooting

### Application Won't Start

**Problem:** `DnsLookupService not found`

**Solution:** Ensure all DNS dependencies are in pom.xml:
```xml
<dependency>
    <groupId>dev.xorcery</groupId>
    <artifactId>xorcery-dns-server</artifactId>
</dependency>
<dependency>
    <groupId>dev.xorcery</groupId>
    <artifactId>xorcery-dns-client</artifactId>
</dependency>
```

**Problem:** `DNS registration failed`

**Solution:** Disable DNS registration in tests:
```yaml
dns.registration.enabled: false
```

### Tests Failing

**Problem:** Port conflicts

**Solution:** Tests use `Sockets.nextFreePort()` to find available ports automatically. If issues persist, ensure no other services are running on ports 8443, 8853, or 7687.

**Problem:** Template not found errors

**Solution:** This is expected until Thymeleaf templates are created. Use tests that don't require templates (like `testApplicationStarts`).

### Build Issues

**Problem:** Compilation errors with package names

**Solution:** Ensure you're using the correct package structure:
- Old: `com.exoreaction.xorcery.*`
- New: `dev.xorcery.*`

**Problem:** Version mismatches

**Solution:** Parent POM version should match: `1.166.10-SNAPSHOT`
Xorcery BOM version: `0.166.9`

### Neo4j Issues

**Problem:** Neo4j won't start

**Solution:** Delete the Neo4j data directory:
```bash
rm -rf neo4j/
```

**Problem:** Can't access Neo4j browser

**Solution:** Embedded Neo4j doesn't expose the browser by default. Use the Neo4j client API or switch to standalone Neo4j.

## Dependencies

### Core Framework
- Xorcery 0.166.9
- HK2 3.1.1 (dependency injection)
- Jersey 3.1.3 (JAX-RS)

### Domain & Persistence
- Xorcery Domain Events
- Neo4j Embedded
- Jackson 2.20.0

### Web & Security
- Jetty Server
- JWT (ES256 algorithm)
- Thymeleaf templates
- SSL/TLS with automatic certificates

### Infrastructure
- DNS Server & Client
- Reactive Streams (WebSocket)
- Log4j2 2.25.2

### Testing
- JUnit 5.6.0
- Xorcery JUnit Extension

## Contributing

To contribute to this example:

1. Follow the existing code structure
2. Add tests for new features
3. Update this README
4. Ensure `mvn clean install` passes

## License

This example is part of the Xorcery project.

## Support

For issues or questions:
- GitHub Issues: https://github.com/Cantara/xorcery-examples
- Xorcery Documentation: https://github.com/Cantara/xorcery

---

**Status:** ✅ Building and Running | 🚧 UI In Progress | 📝 Documentation Complete
