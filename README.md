
# Xorcery Examples

A collection of example applications demonstrating various features and capabilities of the [Xorcery](https://github.com/Cantara/xorcery) framework - a modern, reactive microservices framework for Java.

## Table of Contents

- [About Xorcery](#about-xorcery)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Examples Overview](#examples-overview)
  - [Greeter Example](#greeter-example)
  - [Forum Example](#forum-example)
  - [Streaming Example](#streaming-example)
  - [Persistent Subscriber Example](#persistent-subscriber-example)
- [Building and Running](#building-and-running)
- [Docker Compose Setup](#docker-compose-setup)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Key Technologies](#key-technologies)
- [Migration to Xorcery 0.166.9](#migration-to-xorcery-0166.9)
- [Troubleshooting](#troubleshooting)
- [Documentation](#documentation)
- [Contributing](#contributing)

## About Xorcery

Xorcery is a high-performance, reactive microservices framework built on top of:
- **HK2** for dependency injection
- **Jetty 12** for HTTP/WebSocket server
- **Jersey 3** for REST APIs
- **Project Reactor** for reactive streams
- **Neo4j** for graph database projections
- **OpenTelemetry** for observability

It provides a modular, event-driven architecture with built-in support for:
- Domain-driven design (DDD) with event sourcing
- Reactive streams and backpressure
- WebSocket-based streaming
- Graph database projections
- JWT authentication
- mTLS support
- Service discovery via DNS

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+** for building
- **Docker & Docker Compose** (optional, for running dependencies)

### External Dependencies (Optional)

For the examples, you may need:
- **Neo4j** (graph database for projections) - can run embedded
- **EventStore** (event sourcing database) - for Forum example
- **OpenSearch** (search and analytics) - for Forum example

These can be started using the provided `docker-compose.yaml`.

## Quick Start

```bash
# Clone the repository
git clone https://github.com/Cantara/xorcery-examples.git
cd xorcery-examples

# Build all examples
mvn clean install

# Run the Greeter example
cd xorcery-examples-greeter
mvn exec:java -Dexec.mainClass="dev.xorcery.runner.Main"

# Or run tests
mvn test
```

## Examples Overview

### Greeter Example

**Location:** `xorcery-examples-greeter/`

A simple microservice demonstrating:
- **REST API** with JAX-RS resources
- **Domain events** with event sourcing
- **Neo4j projections** for read models
- **Reactive streams** for event processing
- **Thymeleaf** for server-side rendering
- **WebSocket streaming** for real-time updates

**Key Features:**
- `GreeterApplication` - Command handler for updating greetings
- `GreeterService` - Manages Neo4j projection subscriptions
- `GreeterResource` - REST API endpoint (`/api/greeter`)
- Domain event publishing and projection

**API Endpoints:**
- `GET /api/greeter` - Retrieve current greeting
- `POST /api/greeter` - Update greeting with form parameter

**Running:**
```bash
cd xorcery-examples-greeter
mvn clean install
java -jar target/xorcery-examples-greeter-*.jar
```

**Testing:**
```bash
# Run the test
mvn test

# Or manually test the API
curl https://localhost:8443/api/greeter --insecure
curl -X POST https://localhost:8443/api/greeter -d "greeting=Hello%20Xorcery!" --insecure
```

### Forum Example

**Location:** `xorcery-examples-forum/`

A comprehensive forum application showcasing:
- **JSON:API** compliant REST endpoints
- **Complex domain model** (Posts, Comments, Users)
- **Event sourcing** with EventStore
- **Graph database** for efficient querying
- **JWT authentication**
- **Service discovery** via DNS
- **Thymeleaf templating** for web UI

**Domain Model:**
- Posts with title, content, and metadata
- Comments on posts
- User authentication and authorization

**Key Components:**
- `ForumApplication` - Command handlers
- `ForumModel` - Domain logic
- REST resources for Posts, Comments, Forum operations
- Neo4j projections for read models

**Running:**
```bash
# Start dependencies (EventStore, OpenSearch)
docker-compose up -d xe-eventstore xe-opensearch

# Build and run
cd xorcery-examples-forum
mvn clean install
java -jar target/xorcery-examples-forum-*.jar
```

### Streaming Example

**Location:** `xorcery-examples-streaming/`

Demonstrates **reactive stream processing** with multiple processors in a pipeline:

**Architecture:**
```
Source → Processor1 → Processor2 → Processor3 → Result
```

**Components:**
1. **SourceService** - Publishes a stream of numbers (0-99) via WebSocket
2. **Processor1Service** - Adds a "processor1" field with value "foo"
3. **Processor2Service** - Adds "processor2" field with value + 1
4. **Processor3Service** - Adds "processor3" field with value * 3
5. **ResultService** - Subscribes to the stream and prints results

**Key Features:**
- WebSocket-based streaming with backpressure
- Chain multiple processors dynamically
- Reactive transformations using Project Reactor
- Service composition through configuration
- Demonstrates the reactive streams client/server API

**Running:**
You can run each service independently or configure them to chain together:

```bash
# Run the complete pipeline (all processors configured)
cd xorcery-examples-streaming
mvn clean install
java -jar target/xorcery-examples-streaming-*.jar

# Run tests to see the pipeline in action
mvn test
```

### Persistent Subscriber Example (Neo4j Projections)

**Location:** `xorcery-examples-persistentsubscriber/`

Shows how to create **Neo4j event projections** to handle domain events persistently and build read models.

> **⚠️ Important:** This example has been updated for Xorcery 0.166.9+. The old `xorcery-reactivestreams-persistentsubscriber` module has been **replaced** with the **Neo4j projections API**.

**Key Features:**
- `ExamplePersistentSubscriber` - Implements `Neo4jEventProjection` interface
- Filters and processes specific events (e.g., "CreateApplication" command)
- Writes events to Neo4j within transactions
- Automatic retry and error handling
- State management through Neo4j graph database

**What Changed from Previous Versions:**
- **Old API:** Extended `BasePersistentSubscriber` from `xorcery-reactivestreams-persistentsubscriber`
- **New API:** Implements `Neo4jEventProjection` from `xorcery-neo4j-projections`
- **Benefits:**
    - ✅ Better integration with Neo4j
    - ✅ Transactional guarantees
    - ✅ More flexible event handling
    - ✅ Direct access to Neo4j transaction for complex graph operations

**Use Cases:**
- Building read models from event streams
- Creating graph-based projections from domain events
- Event-driven integrations
- Audit logging
- Real-time analytics

**Running:**
```bash
cd xorcery-examples-persistentsubscriber
mvn clean install
java -jar target/xorcery-examples-persistentsubscriber-*.jar

# Run tests
mvn test
```

**Configuration Example:**
```yaml
# Neo4j Projections
neo4jprojections:
  enabled: true
  projections:
    - name: "examplesubscriber"  # Matches @Service(name = "examplesubscriber")
      stream: "applications"      # Stream to subscribe to
      batchSize: 100              # Events per batch
      timeout: 30                 # Timeout in seconds
```

## Building and Running

### Build All Examples

```bash
mvn clean install
```

### Build Individual Example

```bash
cd xorcery-examples-greeter
mvn clean install
```

### Run with Maven

```bash
mvn exec:java -Dexec.mainClass="dev.xorcery.runner.Main"
```

### Run as JAR

```bash
java -jar target/xorcery-examples-greeter-1.166.1-SNAPSHOT.jar
```

### Run Tests

```bash
# All tests
mvn test

# Specific module
mvn test -pl xorcery-examples-greeter

# Specific test
mvn test -Dtest=GreeterResourceTest
```

### JPackage (Native Installer)

Build native installers for your platform:

```bash
mvn clean install -P jpackage
```

The installer will be in `target/jpackage/`.

## Docker Compose Setup

The repository includes a `docker-compose.yaml` for running external dependencies:

```bash
# Start all services
docker-compose up -d

# Start specific services
docker-compose up -d xe-eventstore xe-opensearch

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Remove volumes (clean slate)
docker-compose down -v
```

**Services included:**
- **xe-eventstore** - EventStore for event sourcing (ports 1113, 2113)
- **xe-opensearch** - OpenSearch for search/analytics (port 9200)
- **xe-opensearch-dashboards** - Kibana-like UI (port 5601)

## Project Structure

```
xorcery-examples/
├── pom.xml                              # Parent POM with dependency versions
├── docker-compose.yaml                  # External dependencies
├── README.md                            # This file
│
├── xorcery-examples-greeter/            # Simple REST + Events example
│   ├── src/main/java/
│   │   ├── com/exoreaction/xorcery/examples/greeter/
│   │   │   ├── GreeterApplication.java      # Command handler
│   │   │   ├── GreeterService.java          # Projection service
│   │   │   ├── commands/
│   │   │   │   └── UpdateGreeting.java
│   │   │   └── resources/api/
│   │   │       └── GreeterResource.java     # REST API
│   │   └── module-info.java
│   ├── src/main/resources/
│   │   └── META-INF/xorcery.yaml            # Configuration
│   └── pom.xml
│
├── xorcery-examples-forum/              # Complex forum application
│   ├── src/main/java/
│   │   └── com/exoreaction/xorcery/examples/forum/
│   │       ├── ForumService.java
│   │       ├── contexts/
│   │       ├── entities/
│   │       ├── model/
│   │       └── resources/
│   └── pom.xml
│
├── xorcery-examples-streaming/          # Stream processing pipeline
│   ├── src/main/java/
│   │   └── com/exoreaction/xorcery/examples/streaming/
│   │       ├── source/
│   │       │   └── SourceService.java       # Stream source
│   │       ├── processors/
│   │       │   ├── ProcessorService.java    # Base processor
│   │       │   ├── Processor1Service.java
│   │       │   ├── Processor2Service.java
│   │       │   └── Processor3Service.java
│   │       └── result/
│   │           └── ResultService.java       # Stream consumer
│   └── pom.xml
│
└── xorcery-examples-persistentsubscriber/  # Neo4j projection example
    ├── src/main/java/
    │   └── com/exoreaction/xorcery/examples/persistentsubscriber/
    │       └── ExamplePersistentSubscriber.java  # Implements Neo4jEventProjection
    ├── src/main/resources/
    │   └── META-INF/xorcery.yaml
    ├── src/test/java/
    │   └── com/exoreaction/xorcery/examples/persistentsubscriber/test/
    │       └── ExamplePersistentSubscriberTest.java
    └── pom.xml
```

## Configuration

Each example has its own `xorcery.yaml` configuration file in `src/main/resources/META-INF/`.

### Key Configuration Properties

```yaml
# Instance configuration
instance:
  id: "{{ instance.host }}"
  host: "{{ CALCULATED.hostName }}"
  domain: "xorcery.test"
  environment: "development"

# Application metadata
application:
  name: "xorcery-example"
  version: "1.166.1-SNAPSHOT"

# Jetty server
jetty.server:
  http:
    enabled: true
    port: 8080
  ssl:
    enabled: true
    port: 8443

# SSL/TLS certificates
certificates:
  enabled: true
  dnsNames:
    - localhost
  ipAddresses:
    - 127.0.0.1

# Keystores
keystores:
  enabled: true
  keystore:
    path: "{{ instance.home }}/keystore.p12"
    password: "password"

# Neo4j configuration
neo4j:
  enabled: true
  uri: "neo4j://localhost:7687"
  # For embedded database
  home: "{{ instance.home }}/neo4j"

# Neo4j Projections
neo4jprojections:
  enabled: true
  projections:
    - name: "examplesubscriber"
      stream: "applications"
      batchSize: 100
      timeout: 30

# Domain Events
domainevents:
  enabled: true
  publisher:
    enabled: true

# Reactive Streams
reactivestreams:
  server:
    enabled: true
  client:
    enabled: true

# OpenTelemetry
opentelemetry:
  enabled: true
  serviceName: "xorcery-example"
  
# Logging
log4j2:
  Configuration:
    status: INFO
    Loggers:
      Root:
        level: INFO
      Logger:
        - name: com.exoreaction.xorcery.examples
          level: DEBUG
```

### Environment Variables

Configuration values can be overridden with environment variables:
- `INSTANCE_NAME` - Instance name
- `INSTANCE_ENVIRONMENT` - Environment (dev, test, prod)
- `JETTY_SERVER_HTTP_PORT` - HTTP port
- `JETTY_SERVER_SSL_PORT` - HTTPS port
- `NEO4J_URI` - Neo4j connection URI
- `EVENTSTORE_HOST` - EventStore host
- `OPENSEARCH_HOST` - OpenSearch host

## Key Technologies

| Technology | Purpose | Version |
|-----------|---------|---------|
| **Xorcery** | Microservices Framework | 0.166.9 |
| **Java** | Programming Language | 21+ |
| **Maven** | Build Tool | 3.8+ |
| **HK2** | Dependency Injection | 3.1.1 |
| **Jersey** | JAX-RS REST API | 3.1.11 |
| **Jetty** | HTTP/WebSocket Server | 12.1.1 |
| **Project Reactor** | Reactive Streams | 3.7.11 |
| **Neo4j** | Graph Database | 5.28.5 |
| **Log4j** | Logging | 2.25.2 |
| **Jackson** | JSON Processing | 2.20.0 |
| **Thymeleaf** | Template Engine | 3.1.2 |
| **JUnit** | Testing | 6.0.0 |
| **OpenTelemetry** | Observability | 1.54.1 |

## Migration to Xorcery 0.166.9

These examples have been migrated from earlier versions of Xorcery to 0.166.9, which includes several **breaking changes**.

### 1. GroupId Change

**All Maven dependencies must be updated:**

```xml
<!-- OLD (pre-0.166.9) -->
<dependency>
    <groupId>com.exoreaction.xorcery</groupId>
    <artifactId>xorcery-core</artifactId>
</dependency>

<!-- NEW (0.166.9+) -->
<dependency>
    <groupId>dev.xorcery</groupId>
    <artifactId>xorcery-core</artifactId>
</dependency>
```

### 2. Package Structure

**All Java imports must be updated:**

```java
// OLD (pre-0.166.9)
import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.core.Xorcery;

// NEW (0.166.9+)
import dev.xorcery.configuration.Configuration;
import dev.xorcery.core.Xorcery;
```

**Find and replace in your IDE:**
- Find: `com.exoreaction.xorcery`
- Replace: `dev.xorcery`

### 3. Persistent Subscriber → Neo4j Projections

**The biggest architectural change is the replacement of persistent subscribers with Neo4j projections:**

#### Old Approach (pre-0.166.9)

```java
@Service(name = "mysubscriber")
public class MySubscriber extends BasePersistentSubscriber {
    
    public MySubscriber(Configuration configuration) {
        super(configuration);
    }
    
    @Override
    public boolean filter(MetadataJsonNode<ArrayNode> event) {
        // Filter events
        return true;
    }
    
    @Override
    public void handle(MetadataJsonNode<ArrayNode> eventsWithMetadata) {
        // Process events
    }
}
```

#### New Approach (0.166.9+)

```java
@Service(name = "mysubscriber")
public class MySubscriber implements Neo4jEventProjection {
    
    private static final Logger logger = LogManager.getLogger(MySubscriber.class);
    
    @Override
    public void write(MetadataEvents events, Transaction transaction) throws Throwable {
        // Filter and process events
        events.data().stream()
                .filter(this::shouldProcess)
                .forEach(event -> handleEvent(event, transaction));
    }
    
    private boolean shouldProcess(DomainEvent event) {
        if (event instanceof JsonDomainEvent jde) {
            JsonNode json = jde.json();
            // Filter logic
            return true;
        }
        return false;
    }
    
    private void handleEvent(DomainEvent event, Transaction transaction) {
        if (event instanceof JsonDomainEvent jde) {
            JsonNode json = jde.json();
            
            // Write to Neo4j using transaction
            String cypher = """
                MERGE (n:MyNode {id: $id})
                SET n.data = $data
                """;
            
            transaction.execute(cypher, Map.of(
                "id", json.get("id").asText(),
                "data", json.get("data").asText()
            ));
        }
    }
}
```

**Key Differences:**
- ✅ **Implements** `Neo4jEventProjection` instead of extending `BasePersistentSubscriber`
- ✅ **Receives** a Neo4j `Transaction` for direct database access
- ✅ **Works with** `MetadataEvents` containing `List<DomainEvent>`
- ✅ **Provides** transactional guarantees
- ✅ **Allows** complex graph operations

**Dependencies Change:**
```xml
<!-- OLD -->
<dependency>
    <groupId>com.exoreaction.xorcery</groupId>
    <artifactId>xorcery-reactivestreams-persistentsubscriber</artifactId>
</dependency>

<!-- NEW -->
<dependency>
    <groupId>dev.xorcery</groupId>
    <artifactId>xorcery-neo4j-projections</artifactId>
</dependency>
<dependency>
    <groupId>dev.xorcery</groupId>
    <artifactId>xorcery-neo4j-embedded</artifactId>
</dependency>
```

### 4. Reactive Streams API Changes

**The client/server API signatures have changed:**

#### ServerWebSocketStreams

```java
// OLD
serverWebSocketStreams.publisher("mystream", JsonNode.class, publisher);

// NEW
serverWebSocketStreams.publisher("mystream", ServerWebSocketOptions.instance(), JsonNode.class, publisher);
```

#### ClientWebSocketStreams

```java
// OLD
clientWebSocketStreams.subscribe(ClientWebSocketOptions.instance(), JsonNode.class)
    .contextWrite(Context.of(ClientWebSocketStreamContext.serverUri, uri))

// NEW
URI serverUri = URI.create("wss://localhost:8443/streams/mystream");
clientWebSocketStreams.subscribe(serverUri, ClientWebSocketOptions.instance(), JsonNode.class)
```

### 5. Module System (module-info.java)

**Module names remain consistent, but ensure correct module references:**

```java
open module xorcery.examples.myapp {
    exports com.exoreaction.xorcery.examples.myapp;

    // Core modules
    requires xorcery.core;
    requires xorcery.configuration.api;
    
    // Domain Events
    requires xorcery.domainevents.api;
    
    // Neo4j Projections (NEW)
    requires xorcery.neo4j.projections;
    requires xorcery.neo4j.shaded;
    
    // Reactive Streams
    requires xorcery.reactivestreams.api;
    requires xorcery.reactivestreams.client;
    requires xorcery.reactivestreams.server;
    
    // OpenTelemetry
    requires xorcery.opentelemetry.sdk;
    
    // Dependency Injection
    requires org.glassfish.hk2.api;
    
    // Logging
    requires org.apache.logging.log4j;
    
    // JSON
    requires com.fasterxml.jackson.databind;
    
    // Testing
    requires static xorcery.junit;
    requires static org.junit.jupiter.api;
}
```

### 6. Configuration Changes

**Neo4j projections configuration:**

```yaml
# OLD (persistent subscriber)
persistentsubscriber:
  subscriptions:
    - name: "mysubscriber"
      stream: "mystream"

# NEW (Neo4j projections)
neo4jprojections:
  enabled: true
  projections:
    - name: "mysubscriber"
      stream: "mystream"
      batchSize: 100
      timeout: 30
```

### Migration Checklist

- [ ] Update all Maven dependencies from `com.exoreaction.xorcery` to `dev.xorcery`
- [ ] Update all Java imports from `com.exoreaction.xorcery.*` to `dev.xorcery.*`
- [ ] Replace `BasePersistentSubscriber` with `Neo4jEventProjection`
- [ ] Update reactive streams client/server API calls
- [ ] Update `module-info.java` to require `xorcery.neo4j.projections`
- [ ] Update configuration from `persistentsubscriber` to `neo4jprojections`
- [ ] Update `ServerWebSocketStreams.publisher()` calls to include `ServerWebSocketOptions`
- [ ] Update `ClientWebSocketStreams.subscribe()` calls to include explicit URI
- [ ] Clean and rebuild: `mvn clean install`
- [ ] Run all tests: `mvn test`

## Troubleshooting

### SSL Certificate Errors

If you get SSL certificate errors when accessing `https://localhost:8443`:

```bash
# Use --insecure flag with curl
curl --insecure https://localhost:8443/api/greeter

# Or import the certificate into your trust store
# Certificate is at: instance.home/keystore.p12
```

**For browsers:** Accept the self-signed certificate warning or import the certificate.

### Port Already in Use

If the default ports are already in use:

```yaml
# Override in xorcery.yaml
jetty.server:
  http:
    port: 8081  # Change to available port
  ssl:
    port: 9443  # Change to available port
```

Or use environment variables:
```bash
export JETTY_SERVER_HTTP_PORT=8081
export JETTY_SERVER_SSL_PORT=9443
```

### Module Not Found Errors

If you get "module not found" errors during compilation:

1. **Check groupId:** Ensure all Xorcery dependencies use `dev.xorcery`
2. **Check module-info.java:** Ensure correct module names (e.g., `xorcery.core`, not `com.exoreaction.xorcery.core`)
3. **Clean build:**
   ```bash
   mvn clean install
   ```
4. **Check POM:** Ensure dependency is in the POM and matches module-info requirement

### Neo4j Database Errors

If Neo4j fails to start:

```bash
# Check disk space
df -h

# Stop any running Neo4j instances
ps aux | grep neo4j
kill <pid>

# Delete database directory (WARNING: deletes all data)
rm -rf instance.home/neo4j/data

# Restart application
mvn clean install
java -jar target/your-app.jar
```

### EventStore Connection Issues

When using EventStore:

```bash
# Ensure EventStore is running
docker-compose up -d xe-eventstore

# Check logs
docker-compose logs -f xe-eventstore

# Check if port is accessible
curl http://localhost:2113

# Access UI
open http://localhost:2113
```

### OpenSearch Connection Issues

```bash
# Ensure OpenSearch is running
docker-compose up -d xe-opensearch

# Check logs
docker-compose logs -f xe-opensearch

# Check if accessible
curl http://localhost:9200

# Access dashboard
open http://localhost:5601
```

### IntelliJ JPMS Testing Issues

If tests don't run in IntelliJ but work with Maven:

**Problem:** IntelliJ has known issues with JPMS (Java Platform Module System) test support, especially with complex module dependencies.

**Solution:** Run tests via Maven instead:

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=GreeterResourceTest

# Run tests for specific module
mvn test -pl xorcery-examples-greeter
```

**Note:** This doesn't affect CI/CD builds (Jenkins), which use Maven.

**Alternative:** Configure IntelliJ Run Configuration:
1. Run → Edit Configurations
2. Add VM options:
   ```
   --add-reads xorcery.examples.myapp=ALL-UNNAMED
   --add-opens xorcery.examples.myapp/com.exoreaction.xorcery.examples.myapp.test=ALL-UNNAMED
   ```

### Compilation Errors After Update

If you get compilation errors after updating to 0.166.9:

```bash
# Clean everything
mvn clean

# Delete IDE files (IntelliJ)
rm -rf .idea/
rm *.iml

# Delete Maven target directories
find . -type d -name target -exec rm -rf {} +

# Reimport project in IDE
# Then rebuild
mvn clean install
```

### Out of Memory Errors

If you get OutOfMemoryError during build:

```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"

# Or set in .mavenrc file
echo "MAVEN_OPTS='-Xmx2048m -XX:MaxPermSize=512m'" > ~/.mavenrc

# Then rebuild
mvn clean install
```

## Documentation

### Xorcery Framework
- **GitHub:** https://github.com/Cantara/xorcery
- **Documentation:** Check the README and Wiki in the main repository
- **Issues:** https://github.com/Cantara/xorcery/issues

### Blog Posts (Getting Started Guides)
Several PDF guides are available in the `docs/getting-started/` directory:
- **Xorcery is born** - Build high-performance microservices
- **Xorcery Examples** - Forum Application walkthrough
- **Xorcery Samples** - Greeter example explained
- **Xorcery implements Reactive Streams** - Streaming architecture
- **Xorcery uses Jetty & Jersey** - mTLS and JWT implementation

### Related Technologies
- **Jersey (JAX-RS):** https://eclipse-ee4j.github.io/jersey/
- **Jetty (Server):** https://eclipse.dev/jetty/
- **Project Reactor:** https://projectreactor.io/
- **Neo4j:** https://neo4j.com/docs/
- **HK2 (DI):** https://javaee.github.io/hk2/
- **OpenTelemetry:** https://opentelemetry.io/docs/
- **Jackson (JSON):** https://github.com/FasterXML/jackson

## Contributing

Contributions are welcome! Please follow these guidelines:

### How to Contribute

1. **Fork the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/xorcery-examples.git
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **Make your changes**
    - Follow existing code style
    - Add tests for new features
    - Update documentation as needed

4. **Test your changes**
   ```bash
   mvn clean test
   ```

5. **Commit your changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Open a Pull Request**
    - Describe your changes
    - Link to related issues
    - Wait for review

### Development Guidelines

- ✅ Follow existing code style and formatting
- ✅ Add tests for new features (aim for 80%+ coverage)
- ✅ Update documentation (README, JavaDoc, etc.)
- ✅ Ensure all tests pass: `mvn clean test`
- ✅ Keep examples simple and focused on one concept
- ✅ Use `dev.xorcery` groupId for all Xorcery dependencies
- ✅ Follow Java 21+ best practices
- ✅ Use meaningful commit messages
- ✅ Update CHANGELOG.md if adding new features

### Code Style

- **Indentation:** 4 spaces (no tabs)
- **Line length:** Max 120 characters
- **Braces:** K&R style
- **Naming:**
    - Classes: `PascalCase`
    - Methods/variables: `camelCase`
    - Constants: `UPPER_SNAKE_CASE`
- **JavaDoc:** Required for public APIs

### Testing Guidelines

- Write unit tests for business logic
- Write integration tests for end-to-end scenarios
- Use meaningful test names: `shouldDoSomethingWhenCondition()`
- Use JUnit 5 features (parameterized tests, nested tests, etc.)
- Mock external dependencies

## Version Compatibility

This examples repository uses:
- **Xorcery:** 0.166.9
- **Java:** 21+
- **Maven:** 3.8+

### Version History

| Version | Xorcery | Java | Key Changes |
|---------|---------|------|-------------|
| 1.166.1 | 0.166.9 | 21+ | Migrated to dev.xorcery groupId, Neo4j projections |
| 1.126.7 | 0.126.x | 17+ | Original version with persistent subscribers |

### Recent Updates (v1.166.1)

- ✅ **Updated all dependencies** from `com.exoreaction.xorcery` to `dev.xorcery`
- ✅ **Updated all package imports** to `dev.xorcery.*`
- ✅ **Migrated persistent subscriber example** to Neo4j projections API
- ✅ **Updated reactive streams** client/server API usage
- ✅ **Fixed all module-info.java** declarations
- 