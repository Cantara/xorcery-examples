# Xorcery Forum Example

A comprehensive forum application demonstrating advanced features of the Xorcery framework, including event sourcing, JSON:API compliant REST endpoints, Neo4j graph projections, and authentication.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Domain Model](#domain-model)
- [Technology Stack](#technology-stack)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Overview

The Forum example is a fully-featured discussion forum application that demonstrates:

- **Event Sourcing** - All state changes are captured as domain events
- **CQRS Pattern** - Commands for writes, Neo4j projections for reads
- **JSON:API Standard** - RESTful API following JSON:API specification
- **Graph Database** - Neo4j for efficient relationship queries
- **Authentication** - JWT-based authentication and authorization
- **Real-time Updates** - WebSocket streaming for live notifications
- **Service Discovery** - DNS-based service registration

This example is ideal for understanding how to build production-grade, event-sourced microservices with Xorcery.

## Features

### Core Features

- ✅ **Create and manage forum posts**
  - Rich text content support
  - Post metadata (author, timestamps, tags)
  - Post categorization

- ✅ **Threaded comments**
  - Nested comment threads
  - Comment replies and mentions
  - Comment voting/reactions

- ✅ **User management**
  - User registration and profiles
  - JWT authentication
  - Role-based access control (RBAC)

- ✅ **Search and filtering**
  - Full-text search via OpenSearch
  - Filter by author, date, tags
  - Pagination support

- ✅ **Event sourcing**
  - Complete audit trail
  - Event replay capability
  - Time-travel queries

### Technical Features

- ✅ **JSON:API compliant REST API**
- ✅ **Neo4j graph projections** for efficient queries
- ✅ **EventStore integration** for event persistence
- ✅ **OpenTelemetry** for observability
- ✅ **Thymeleaf** templates for web UI
- ✅ **WebSocket streaming** for real-time updates
- ✅ **mTLS support** for secure communication

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Clients                              │
│  (Web Browser, Mobile App, API Consumer)                    │
└─────────────────────┬───────────────────────────────────────┘
│ HTTPS/WSS
▼
┌─────────────────────────────────────────────────────────────┐
│                    Jetty Server (12.1.1)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Jersey       │  │ WebSocket    │  │ Thymeleaf    │     │
│  │ REST API     │  │ Streaming    │  │ Templates    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────┬───────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│                   Forum Application                          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Command Handlers                         │  │
│  │  • CreatePost    • UpdatePost    • DeletePost        │  │
│  │  • AddComment    • EditComment   • DeleteComment     │  │
│  │  • RegisterUser  • LoginUser     • UpdateProfile     │  │
│  └──────────────────────────────────────────────────────┘  │
│                      │                                       │
│                      ▼ Domain Events                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Domain Event Publisher                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────────┘
│
┌───────────┼───────────┐
▼           ▼           ▼
┌──────────┐ ┌────────┐ ┌─────────┐
│EventStore│ │ Neo4j  │ │OpenSearch│
│ (Events) │ │(Reads) │ │(Search)  │
└──────────┘ └────────┘ └─────────┘
```

### Event Sourcing Flow

```
1. Command Received (POST /api/posts)
   └─→ ForumApplication validates command
   └─→ Business logic applied
   └─→ Domain events generated
   └─→ Events published to EventStore
   └─→ Neo4j projection updates read model
   └─→ Response returned to client
```

### Domain Events

Key events in the system:

- **Post Events:** `PostCreated`, `PostUpdated`, `PostDeleted`, `PostPublished`
- **Comment Events:** `CommentAdded`, `CommentEdited`, `CommentDeleted`
- **User Events:** `UserRegistered`, `UserLoggedIn`, `ProfileUpdated`
- **Moderation Events:** `PostFlagged`, `CommentModerated`, `UserBanned`

## Prerequisites

### Required

- **Java 21+**
- **Maven 3.8+**

### Optional (External Services)

- **EventStore** - For event persistence (can use Docker)
- **OpenSearch** - For full-text search (can use Docker)
- **Neo4j** - For graph queries (embedded or external)

## Quick Start

### 1. Start External Dependencies (Docker)

```bash
# From the repository root
docker-compose up -d xe-eventstore xe-opensearch

# Verify services are running
docker-compose ps
```

### 2. Build the Application

```bash
cd xorcery-examples-forum
mvn clean install
```

### 3. Run the Application

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.forum.Main"

# Or using the JAR
java -jar target/xorcery-examples-forum-1.166.1-SNAPSHOT.jar
```

### 4. Access the Application

- **Web UI:** https://localhost:8443/
- **API Base:** https://localhost:8443/api/
- **Health Check:** https://localhost:8443/status

**Note:** Accept the self-signed SSL certificate when accessing HTTPS endpoints.

### 5. Quick Test

```bash
# Create a post
curl -X POST https://localhost:8443/api/posts \
  -H "Content-Type: application/vnd.api+json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "data": {
      "type": "posts",
      "attributes": {
        "title": "Welcome to Xorcery Forum!",
        "content": "This is my first post.",
        "author": "admin"
      }
    }
  }' \
  --insecure

# List posts
curl https://localhost:8443/api/posts --insecure

# Get specific post
curl https://localhost:8443/api/posts/1 --insecure
```

## Configuration

### Main Configuration File

**Location:** `src/main/resources/META-INF/xorcery.yaml`

```yaml
# Instance Configuration
instance:
  id: "forum-{{ instance.host }}"
  domain: "xorcery.test"
  environment: "development"

# Application
application:
  name: "xorcery-forum"
  version: "1.166.1-SNAPSHOT"

# Jetty Server
jetty.server:
  http:
    enabled: true
    port: 8080
  ssl:
    enabled: true
    port: 8443

# EventStore Configuration
eventstore:
  enabled: true
  host: "localhost"
  port: 1113
  connectionString: "esdb://localhost:2113?tls=false"

# Neo4j Configuration
neo4j:
  enabled: true
  uri: "neo4j://localhost:7687"
  # Use embedded for development
  embedded: true
  home: "{{ instance.home }}/neo4j"

# Neo4j Projections
neo4jprojections:
  enabled: true
  projections:
    - name: "forum-posts"
      stream: "forum-posts"
    - name: "forum-comments"
      stream: "forum-comments"
    - name: "forum-users"
      stream: "forum-users"

# OpenSearch Configuration
opensearch:
  enabled: true
  host: "localhost"
  port: 9200
  index: "forum-posts"

# JWT Authentication
jwt:
  enabled: true
  issuer: "xorcery-forum"
  secret: "{{ SECRETS.jwt.secret }}"
  expirationMinutes: 60

# Domain Events
domainevents:
  enabled: true
  publisher:
    enabled: true

# Logging
log4j2:
  Configuration:
    Loggers:
      Logger:
        - name: com.exoreaction.xorcery.examples.forum
          level: DEBUG
```

### Environment-Specific Configuration

Create `xorcery-production.yaml` for production overrides:

```yaml
jetty.server:
  ssl:
    port: 443

eventstore:
  host: "eventstore.production.local"
  connectionString: "esdb://eventstore.production.local:2113?tls=true"

neo4j:
  embedded: false
  uri: "neo4j://neo4j.production.local:7687"
  username: "neo4j"
  password: "{{ SECRETS.neo4j.password }}"
```

## API Documentation

### Base URL

```
https://localhost:8443/api/
```

### Authentication

All write operations require a JWT token:

```bash
# Login to get token
curl -X POST https://localhost:8443/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret"}' \
  --insecure

# Use token in subsequent requests
curl https://localhost:8443/api/posts \
  -H "Authorization: Bearer eyJhbGc..." \
  --insecure
```

### Posts API

#### List Posts

```http
GET /api/posts
GET /api/posts?page[offset]=0&page[limit]=10
GET /api/posts?filter[author]=john
GET /api/posts?sort=-createdAt
```

**Response (JSON:API):**
```json
{
  "data": [
    {
      "type": "posts",
      "id": "post-123",
      "attributes": {
        "title": "Welcome Post",
        "content": "Hello, world!",
        "author": "admin",
        "createdAt": "2025-01-08T10:00:00Z",
        "updatedAt": "2025-01-08T10:00:00Z"
      },
      "relationships": {
        "comments": {
          "links": {
            "related": "/api/posts/post-123/comments"
          }
        }
      }
    }
  ],
  "links": {
    "self": "/api/posts?page[offset]=0&page[limit]=10",
    "next": "/api/posts?page[offset]=10&page[limit]=10"
  }
}
```

#### Get Single Post

```http
GET /api/posts/{postId}
```

#### Create Post

```http
POST /api/posts
Content-Type: application/vnd.api+json
Authorization: Bearer {token}

{
  "data": {
    "type": "posts",
    "attributes": {
      "title": "New Post Title",
      "content": "Post content here...",
      "tags": ["xorcery", "example"]
    }
  }
}
```

#### Update Post

```http
PATCH /api/posts/{postId}
Content-Type: application/vnd.api+json
Authorization: Bearer {token}

{
  "data": {
    "type": "posts",
    "id": "post-123",
    "attributes": {
      "title": "Updated Title"
    }
  }
}
```

#### Delete Post

```http
DELETE /api/posts/{postId}
Authorization: Bearer {token}
```

### Comments API

#### List Comments for Post

```http
GET /api/posts/{postId}/comments
```

#### Add Comment

```http
POST /api/posts/{postId}/comments
Content-Type: application/vnd.api+json
Authorization: Bearer {token}

{
  "data": {
    "type": "comments",
    "attributes": {
      "content": "Great post!",
      "parentId": null
    }
  }
}
```

### Users API

#### Get Current User

```http
GET /api/users/me
Authorization: Bearer {token}
```

#### Register User

```http
POST /api/users/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "user@example.com",
  "password": "securepassword"
}
```

## Domain Model

### Aggregates

#### Post Aggregate

```java
public class Post {
    private String id;
    private String title;
    private String content;
    private String authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private PostStatus status; // DRAFT, PUBLISHED, ARCHIVED
}
```

#### Comment Aggregate

```java
public class Comment {
    private String id;
    private String postId;
    private String content;
    private String authorId;
    private String parentCommentId; // For nested threads
    private LocalDateTime createdAt;
}
```

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

### Neo4j Graph Model

```
(User)-[:AUTHORED]->(Post)
(User)-[:AUTHORED]->(Comment)
(Comment)-[:ON_POST]->(Post)
(Comment)-[:REPLIES_TO]->(Comment)
(Post)-[:TAGGED_WITH]->(Tag)
(User)-[:FOLLOWS]->(User)
```

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Xorcery | 0.166.9 |
| Language | Java | 21+ |
| DI Container | HK2 | 3.1.1 |
| REST API | Jersey (JAX-RS) | 3.1.11 |
| Web Server | Jetty | 12.1.1 |
| Reactive | Project Reactor | 3.7.11 |
| Event Store | EventStoreDB | Latest |
| Graph DB | Neo4j | 5.28.5 |
| Search | OpenSearch | Latest |
| Templates | Thymeleaf | 3.1.2 |
| JSON | Jackson | 2.20.0 |
| Logging | Log4j | 2.25.2 |
| Telemetry | OpenTelemetry | 1.54.1 |

## Development

### Project Structure

```
xorcery-examples-forum/
├── src/main/java/com/exoreaction/xorcery/examples/forum/
│   ├── ForumService.java           # Main service
│   ├── Main.java                    # Entry point
│   ├── contexts/                    # Bounded contexts
│   │   ├── PostContext.java
│   │   ├── CommentContext.java
│   │   └── UserContext.java
│   ├── entities/                    # Domain entities
│   │   ├── Post.java
│   │   ├── Comment.java
│   │   └── User.java
│   ├── model/                       # Domain model
│   │   ├── ForumApplication.java   # Command handlers
│   │   ├── ForumModel.java         # Business logic
│   │   └── ForumProjection.java    # Neo4j projection
│   ├── resources/                   # REST API resources
│   │   ├── api/
│   │   │   ├── PostsResource.java
│   │   │   ├── CommentsResource.java
│   │   │   └── UsersResource.java
│   │   └── web/
│   │       └── ForumWebResource.java
│   └── login/                       # Authentication
│       ├── LoginResource.java
│       └── JwtService.java
├── src/main/resources/
│   ├── META-INF/
│   │   └── xorcery.yaml            # Configuration
│   └── templates/                   # Thymeleaf templates
│       ├── index.html
│       ├── post.html
│       └── comment.html
└── pom.xml
```

### Adding New Features

1. **Define domain events** in the model package
2. **Implement command handlers** in `ForumApplication`
3. **Update Neo4j projection** in `ForumProjection`
4. **Create REST endpoints** in resources package
5. **Add tests** for new functionality

### Running in Development Mode

```bash
# With auto-reload (requires JRebel or similar)
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.forum.Main"

# With debugger
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.forum.Main" \
  -Dexec.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

## Testing

### Run All Tests

```bash
mvn test
```

### Unit Tests

Test domain logic in isolation:

```bash
mvn test -Dtest=ForumModelTest
```

### Integration Tests

Test with embedded Neo4j:

```bash
mvn test -Dtest=ForumIntegrationTest
```

### API Tests

Test REST endpoints:

```bash
mvn test -Dtest=PostsResourceTest
```

### Manual Testing

Use the provided Postman collection (if available) or curl:

```bash
# Health check
curl https://localhost:8443/status --insecure

# API test
curl https://localhost:8443/api/posts --insecure
```

## Deployment

### Build Production JAR

```bash
mvn clean package
```

### Build Docker Image

```bash
# Using provided Dockerfile
docker build -t xorcery-forum:latest .

# Run container
docker run -p 8443:8443 \
  -e NEO4J_URI=neo4j://neo4j:7687 \
  -e EVENTSTORE_HOST=eventstore \
  xorcery-forum:latest
```

### Using Docker Compose

```bash
# Start entire stack
docker-compose -f src/main/docker/docker-compose.yaml up -d
```

### Environment Variables

```bash
# Required in production
export INSTANCE_ENVIRONMENT=production
export JETTY_SERVER_SSL_PORT=443
export NEO4J_URI=neo4j://prod-neo4j:7687
export NEO4J_PASSWORD=secret
export EVENTSTORE_HOST=prod-eventstore
export OPENSEARCH_HOST=prod-opensearch
export JWT_SECRET=your-secret-key
```

## Troubleshooting

### Application Won't Start

```bash
# Check logs
tail -f logs/xorcery-forum.log

# Check port conflicts
lsof -i :8443
```

### EventStore Connection Errors

```bash
# Verify EventStore is running
curl http://localhost:2113/web/index.html

# Check connection string in configuration
```

### Neo4j Errors

```bash
# Access Neo4j browser
open http://localhost:7474

# Check database status
# In Neo4j browser: CALL dbms.components()
```

### JWT Token Issues

```bash
# Verify token is valid
jwt decode eyJhbGc...

# Check token expiration
# Token includes "exp" claim
```

### Performance Issues

- Check Neo4j query performance: Enable query logging
- Monitor EventStore: Check dashboard at http://localhost:2113
- Enable OpenTelemetry tracing for detailed metrics

## License

Apache License 2.0

## Support

- **Issues:** https://github.com/Cantara/xorcery-examples/issues
- **Xorcery Docs:** https://github.com/Cantara/xorcery

---

**Built with Xorcery - The Modern Microservices Framework**
