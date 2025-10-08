
# Xorcery Greeter Example

A simple yet comprehensive microservice demonstrating the core features of the Xorcery framework, including REST APIs, domain events, event sourcing, Neo4j projections, and reactive streams.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Domain Model](#domain-model)
- [How It Works](#how-it-works)
- [Testing](#testing)
- [Deployment](#deployment)
- [Customization](#customization)
- [Troubleshooting](#troubleshooting)

## Overview

The Greeter example is a "Hello World" application on steroids. It demonstrates how to build a production-ready microservice with Xorcery that includes:

- **Command/Query Separation** - Commands update state, queries read from projections
- **Event Sourcing** - All state changes captured as events
- **Neo4j Projections** - Fast read models in a graph database
- **REST API** - JSON:API compliant endpoints
- **Web UI** - Server-side rendered HTML with Thymeleaf
- **Reactive Streams** - Real-time event streaming via WebSocket

This is the perfect starting point for learning Xorcery or as a template for new services.

## Features

### Core Functionality

- ✅ **Store and retrieve greetings** - Simple key-value storage with a twist
- ✅ **Update greetings** - Change the greeting message
- ✅ **Event history** - Complete audit trail of all changes
- ✅ **Real-time updates** - WebSocket streaming of greeting changes

### Technical Features

- ✅ **REST API** - GET and POST endpoints
- ✅ **JSON:API support** - Standard JSON:API format
- ✅ **Thymeleaf templates** - HTML views
- ✅ **Domain events** - Event sourcing pattern
- ✅ **Neo4j projection** - Graph database read model
- ✅ **WebSocket streaming** - Live event feed
- ✅ **SSL/TLS** - Secure HTTPS endpoints
- ✅ **OpenTelemetry** - Built-in observability
- ✅ **Health checks** - `/status` endpoint

## Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         Client                               │
│  (Browser, curl, API client)                                │
└─────────────────────┬───────────────────────────────────────┘
│ HTTPS
▼
┌─────────────────────────────────────────────────────────────┐
│              Jetty Server (Port 8443)                        │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ GreeterResource  │  │ Thymeleaf Views  │                │
│  │ (REST API)       │  │ (HTML)           │                │
│  └────────┬─────────┘  └──────────────────┘                │
└───────────┼──────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│              GreeterApplication                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         Command Handler: UpdateGreeting               │  │
│  │  1. Validate command                                  │  │
│  │  2. Apply business logic                              │  │
│  │  3. Generate domain event: UpdatedGreeting            │  │
│  │  4. Publish event                                     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────────┘
│ Domain Event
▼
┌─────────────────────────────────────────────────────────────┐
│           Domain Event Publisher                             │
│  • Publishes to event stream                                │
│  • Triggers projections                                     │
└─────────────────────┬───────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│              GreeterService                                  │
│  • Neo4j Projection Subscription                            │
│  • Updates graph database with current greeting             │
│  • Provides fast read model                                 │
└─────────────────────┬───────────────────────────────────────┘
│
▼
┌────────┐
│ Neo4j  │
│ (Read) │
└────────┘
```

### Event Flow

```
1. HTTP POST /api/greeter (greeting="Hello Xorcery")
   ↓
2. GreeterResource validates input
   ↓
3. GreeterApplication.handle(UpdateGreeting)
   ↓
4. Domain event created: UpdatedGreeting
   ↓
5. Event published to domain event stream
   ↓
6. GreeterService (Neo4j projection) receives event
   ↓
7. Neo4j updated with new greeting
   ↓
8. HTTP 200 OK response returned
```

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **Neo4j** (optional - embedded by default)

## Quick Start

### 1. Build the Application

```bash
cd xorcery-examples-greeter
mvn clean install
```

### 2. Run the Application

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.greeter.Main"

# Or using the JAR
java -jar target/xorcery-examples-greeter-1.166.1-SNAPSHOT.jar
```

### 3. Access the Application

**Web UI:**
```bash
open https://localhost:8443/api/greeter
# Accept the self-signed certificate
```

**API:**
```bash
# Get current greeting
curl https://localhost:8443/api/greeter --insecure

# Update greeting
curl -X POST https://localhost:8443/api/greeter \
  -d "greeting=Hello%20from%20Xorcery!" \
  --insecure
```

### 4. View in Browser

Navigate to `https://localhost:8443/api/greeter` and you'll see:
- Current greeting displayed
- Form to update the greeting
- Real-time updates when the greeting changes

## Configuration

### Main Configuration

**File:** `src/main/resources/META-INF/xorcery.yaml`

```yaml
# Instance configuration
instance:
  id: "greeter-{{ instance.host }}"
  host: "{{ CALCULATED.hostName }}"
  domain: "xorcery.test"
  environment: "development"

# Application metadata
application:
  name: "xorcery-greeter"
  version: "1.166.1-SNAPSHOT"

# Jetty server
jetty.server:
  http:
    enabled: false  # Only HTTPS
  ssl:
    enabled: true
    port: 8443
    sniRequired: false
    sniHostCheck: false

# SSL certificates
certificates:
  enabled: true
  dnsNames:
    - localhost
    - "{{ instance.host }}"
  ipAddresses:
    - 127.0.0.1
    - "{{ instance.ip }}"

# Neo4j (embedded)
neo4j:
  enabled: true
  uri: "neo4j://localhost:7687"
  embedded: true
  home: "{{ instance.home }}/neo4j"

# Domain events
domainevents:
  enabled: true
  publisher:
    enabled: true

# Neo4j projections
neo4jprojections:
  enabled: true

# Reactive streams
reactivestreams:
  server:
    enabled: true
  client:
    enabled: true

# Logging
log4j2:
  Configuration:
    status: INFO
    Loggers:
      Root:
        level: INFO
      Logger:
        - name: com.exoreaction.xorcery.examples.greeter
          level: DEBUG
```

### Override Configuration

Create `xorcery-local.yaml` for local development overrides:

```yaml
# Use HTTP for development
jetty.server:
  http:
    enabled: true
    port: 8080
  ssl:
    enabled: false

# Debug logging
log4j2:
  Configuration:
    Loggers:
      Root:
        level: DEBUG
```

## API Documentation

### Base URL

```
https://localhost:8443/api/
```

### Endpoints

#### Get Greeting

```http
GET /api/greeter
Accept: application/json
```

**Response:**
```json
{
  "greeting": "Hello World"
}
```

**Response (HTML):**
```http
GET /api/greeter
Accept: text/html
```
Returns Thymeleaf-rendered HTML page.

#### Update Greeting

```http
POST /api/greeter
Content-Type: application/x-www-form-urlencoded

greeting=Hello%20Xorcery!
```

**Response:**
```json
{
  "message": "Greeting updated successfully",
  "greeting": "Hello Xorcery!"
}
```

### Health Check

```http
GET /status
```

Returns system health status and metrics.

## Domain Model

### Commands

#### UpdateGreeting

```java
public record UpdateGreeting(String newGreeting) {
    // Command to update the greeting message
}
```

### Events

#### UpdatedGreeting

```java
{
  "eventType": "UpdatedGreeting",
  "commandType": "UpdateGreeting",
  "timestamp": 1704708000000,
  "metadata": {
    "domain": "greeter",
    "commandName": "UpdateGreeting"
  },
  "updated": {
    "Greeter": "greeter"
  },
  "updatedAttributes": {
    "greeting": "Hello Xorcery!"
  }
}
```

### Aggregate

#### Greeter

```java
public class Greeter {
    private String id = "greeter";  // Singleton
    private String greeting;
    private LocalDateTime updatedAt;
}
```

Stored in Neo4j as:
```cypher
(:Greeter {id: "greeter", greeting: "Hello World", updatedAt: "2025-01-08T..."})
```

## How It Works

### 1. Command Processing

When you POST to `/api/greeter`:

```java
@POST
public Response updateGreeting(@FormParam("greeting") String newGreeting) {
    UpdateGreeting command = new UpdateGreeting(newGreeting);
    CompletableFuture<Metadata> result = application.handle(command);
    return Response.ok().build();
}
```

### 2. Domain Event Generation

```java
public class GreeterApplication {
    private List<DomainEvent> handle(UpdateGreeting updateGreeting) {
        return Collections.singletonList(
            JsonDomainEvent.event("UpdatedGreeting")
                .updated("Greeter", "greeter")
                .updatedAttribute("greeting", updateGreeting.newGreeting())
                .build()
        );
    }
}
```

### 3. Event Publishing

```java
MetadataEvents metadataEvents = new MetadataEvents(metadata, events);
domainEventPublisher.publish(metadataEvents);
```

### 4. Neo4j Projection

```java
@Service(name = "greeter")
public class GreeterService {
    // Subscribes to domain events
    // Updates Neo4j when UpdatedGreeting event received
    // Maintains current state for fast reads
}
```

### 5. Query from Projection

```java
public CompletableFuture<String> get(String name) {
    return graphDatabase.execute(
        "MATCH (greeter:Greeter {id:$id}) RETURN greeter.greeting as greeting",
        Map.of("id", "greeter"), 
        30
    );
}
```

## Testing

### Unit Tests

Test domain logic:

```bash
mvn test -Dtest=GreeterApplicationTest
```

### Integration Tests

Test with embedded Neo4j:

```bash
mvn test -Dtest=GreeterResourceTest
```

**Example test:**
```java
@Test
public void testGreeterResource() throws Exception {
    // Get initial greeting
    Response response = client.target("https://localhost:" + port + "/api/greeter")
        .request(MediaType.APPLICATION_JSON)
        .get();
    
    assertEquals(200, response.getStatus());
    
    // Update greeting
    Form form = new Form();
    form.param("greeting", "Test Greeting");
    
    response = client.target("https://localhost:" + port + "/api/greeter")
        .request()
        .post(Entity.form(form));
    
    assertEquals(200, response.getStatus());
    
    // Verify update
    response = client.target("https://localhost:" + port + "/api/greeter")
        .request(MediaType.APPLICATION_JSON)
        .get();
    
    JsonNode result = response.readEntity(JsonNode.class);
    assertEquals("Test Greeting", result.get("greeting").asText());
}
```

### Manual Testing

```bash
# Start the application
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.greeter.Main"

# In another terminal:
# Test GET
curl https://localhost:8443/api/greeter --insecure

# Test POST
curl -X POST https://localhost:8443/api/greeter \
  -d "greeting=Hello%20Xorcery!" \
  --insecure

# Test HTML view
curl https://localhost:8443/api/greeter \
  -H "Accept: text/html" \
  --insecure
```

## Deployment

### Build Production JAR

```bash
mvn clean package
```

### Run in Production

```bash
java -jar target/xorcery-examples-greeter-1.166.1-SNAPSHOT.jar
```

### Build Native Installer (JPackage)

```bash
mvn clean install -P jpackage
```

Installer will be in `target/jpackage/`:
- **Linux:** `.deb` or `.rpm`
- **Windows:** `.msi` or `.exe`
- **macOS:** `.pkg` or `.dmg`

### Docker Deployment

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/xorcery-examples-greeter-*.jar app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and run:**
```bash
docker build -t xorcery-greeter:latest .
docker run -p 8443:8443 xorcery-greeter:latest
```

### Environment Variables

```bash
# Override configuration via environment variables
export INSTANCE_ENVIRONMENT=production
export JETTY_SERVER_SSL_PORT=443
export LOG_LEVEL=INFO

java -jar target/xorcery-examples-greeter-*.jar
```

## Customization

### Add New Commands

1. **Create command record:**
```java
public record SetGreetingLanguage(String language, String greeting) {}
```

2. **Add handler in GreeterApplication:**
```java
private List<DomainEvent> handle(SetGreetingLanguage cmd) {
    return List.of(
        JsonDomainEvent.event("GreetingLanguageChanged")
            .updated("Greeter", "greeter")
            .updatedAttribute("language", cmd.language())
            .updatedAttribute("greeting", cmd.greeting())
            .build()
    );
}
```

3. **Add REST endpoint:**
```java
@POST
@Path("/language")
public Response setLanguage(
    @FormParam("language") String language,
    @FormParam("greeting") String greeting
) {
    application.handle(new SetGreetingLanguage(language, greeting));
    return Response.ok().build();
}
```

### Add Neo4j Queries

Extend `GreeterApplication` with new query methods:

```java
public CompletableFuture<List<String>> getGreetingHistory() {
    return graphDatabase.execute(
        "MATCH (g:Greeter) RETURN g.greeting ORDER BY g.updatedAt DESC LIMIT 10",
        Map.of(),
        30
    ).thenApply(result -> {
        List<String> history = new ArrayList<>();
        result.forEach(record -> 
            history.add(record.get("g.greeting").toString())
        );
        return history;
    });
}
```

## Troubleshooting

### Port 8443 Already in Use

```bash
# Find process using port
lsof -i :8443

# Change port in xorcery.yaml
jetty.server.ssl.port: 9443
```

### SSL Certificate Error

**Problem:** Browser shows "Your connection is not private"

**Solution:** This is expected with self-signed certificates in development.
- Click "Advanced" → "Proceed to localhost (unsafe)"
- Or use curl with `--insecure` flag
- Or import the certificate from `home/keystore.p12` to your system trust store

### Neo4j Embedded Database Error

```bash
# Delete database directory
rm -rf home/neo4j/

# Restart application
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.greeter.Main"
```

### Application Won't Start

```bash
# Check Java version
java -version  # Should be 21+

# Clean build
mvn clean install

# Check logs
tail -f logs/xorcery-greeter.log
```

### Changes Not Reflected

```bash
# Clear Neo4j state
rm -rf home/neo4j/

# Rebuild
mvn clean install

# Run
java -jar target/xorcery-examples-greeter-*.jar
```

## Key Concepts Demonstrated

This example demonstrates these Xorcery patterns:

- ✅ **Command Pattern** - `UpdateGreeting` command
- ✅ **Event Sourcing** - `UpdatedGreeting` events
- ✅ **CQRS** - Commands via POST, queries via GET
- ✅ **Projection** - Neo4j read model
- ✅ **REST API** - JAX-RS with Jersey
- ✅ **Content Negotiation** - JSON and HTML responses
- ✅ **Reactive Streams** - Event publishing and subscription
- ✅ **Dependency Injection** - HK2-based DI
- ✅ **Configuration** - YAML-based configuration
- ✅ **SSL/TLS** - Secure HTTPS by default

## Next Steps

After understanding this example, try:

1. **Streaming Example** - Learn reactive stream processing
2. **Forum Example** - See a more complex domain model
3. **Persistent Subscriber Example** - Understand Neo4j projections in depth

## License

Apache License 2.0

## Support

- **Issues:** https://github.com/Cantara/xorcery-examples/issues
- **Xorcery Docs:** https://github.com/Cantara/xorcery

---

**Happy coding with Xorcery!** 🚀
