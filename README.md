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
- [Troubleshooting](#troubleshooting)
- [Documentation](#documentation)
- [Contributing](#contributing)

## About Xorcery

Xorcery is a high-performance, reactive microservices framework built on top of:
- **HK2** for dependency injection
- **Jetty** for HTTP/WebSocket server
- **Jersey** for REST APIs
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

For the full Forum example, you may need:
- **EventStore** (event sourcing database)
- **OpenSearch** (search and analytics)

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
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.greeter.Main"

# Or run the test
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

Demonstrates **reactive stream processing** with multiple processors:

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

**Running:**
You can run each service independently or configure them to chain together:

```bash
# Run source
cd xorcery-examples-streaming
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.streaming.source.Main"

# Run processors and result in separate terminals
# (Configure endpoints in xorcery.yaml)
```

### Persistent Subscriber Example

**Location:** `xorcery-examples-persistentsubscriber/`

Shows how to create **persistent subscribers** for event streams:

**Key Features:**
- `ExamplePersistentSubscriber` - Filters and processes specific events
- Automatic retry and error handling
- State management for subscription position
- Filters events by command type (e.g., "CreateApplication")

**Use Cases:**
- Building read models from event streams
- Event-driven integrations
- Audit logging
- Real-time notifications

**Running:**
```bash
cd xorcery-examples-persistentsubscriber
mvn clean install
java -jar target/xorcery-examples-persistentsubscriber-*.jar
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
mvn exec:java -Dexec.mainClass="com.exoreaction.xorcery.examples.greeter.Main"
```

### Run as JAR

```bash
java -jar target/xorcery-examples-greeter-1.126.7-SNAPSHOT.jar
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
```

**Services included:**
- **xe-eventstore** - EventStore for event sourcing (ports 1113, 2113)
- **xe-opensearch** - OpenSearch for search/analytics (port 9200)
- **xe-opensearch-dashboards** - Kibana-like UI (port 5601)

## Project Structure

```
xorcery-examples/
├── pom.xml                              # Parent POM with versions
├── docker-compose.yaml                  # External dependencies
├── README.md                            # This file
├── xorcery-examples-greeter/            # Simple REST + Events example
│   ├── src/main/java/
│   │   ├── com/exoreaction/xorcery/examples/greeter/
│   │   │   ├── GreeterApplication.java
│   │   │   ├── GreeterService.java
│   │   │   ├── commands/
│   │   │   └── resources/api/
│   │   └── module-info.java
│   ├── src/main/resources/
│   │   └── xorcery.yaml                # Configuration
│   └── pom.xml
├── xorcery-examples-forum/              # Complex forum application
│   ├── src/main/java/
│   │   └── com/exoreaction/xorcery/examples/forum/
│   │       ├── ForumService.java
│   │       ├── contexts/
│   │       ├── entities/
│   │       ├── model/
│   │       └── resources/
│   └── pom.xml
├── xorcery-examples-streaming/          # Stream processing pipeline
│   ├── src/main/java/
│   │   └── com/exoreaction/xorcery/examples/streaming/
│   │       ├── source/
│   │       ├── processors/
│   │       └── result/
│   └── pom.xml
└── xorcery-examples-persistentsubscriber/  # Event subscriber example
    └── src/main/java/
        └── com/exoreaction/xorcery/examples/persistentsubscriber/
```

## Configuration

Each example has its own `xorcery.yaml` configuration file in `src/main/resources/`.

### Key Configuration Properties

```yaml
# Instance configuration
instance:
  name: "greeter"
  domain: "local"
  host: "localhost"

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
  dnsNames:
    - localhost
  ipAddresses:
    - 127.0.0.1

# Keystores
keystores:
  enabled: true
  keystore:
    path: "{{ home }}/keystore.p12"
    password: "password"

# Domain events
domainevents:
  eventstore: null  # or EventStore connection string
  projections: "{{reactivestreams.server.uri}}projections/greeter"

# Logging
log4j2:
  Configuration:
    Loggers:
      Root:
        level: info
```

### Environment Variables

Configuration values can be overridden with environment variables:
- `INSTANCE_NAME`
- `JETTY_SERVER_HTTP_PORT`
- `EVENTSTORE_HOST`
- `OPENSEARCH_HOST`

## Key Technologies

| Technology | Purpose | Version |
|-----------|---------|---------|
| Xorcery | Microservices Framework | 0.132.5 |
| Java | Programming Language | 17+ |
| HK2 | Dependency Injection | 3.1.1 |
| Jersey | JAX-RS REST API | 3.1.3 |
| Jetty | HTTP/WebSocket Server | 12.0.14 |
| Project Reactor | Reactive Streams | 3.6.11 |
| Neo4j | Graph Database | 5.23.0 |
| Log4j | Logging | 2.24.3 |
| Jackson | JSON Processing | 2.18.1 |
| Thymeleaf | Template Engine | 3.1.2 |

## Troubleshooting

### SSL Certificate Errors

If you get SSL certificate errors when accessing `https://localhost:8443`:
- Use `--insecure` flag with curl
- Or import the generated certificate into your browser/system trust store
- The certificate is generated at runtime in `home/keystore.p12`

### Port Already in Use

If the default ports are already in use, override them in `xorcery.yaml`:

```yaml
jetty.server:
  ssl:
    port: 9443  # Change to available port
```

### Module Not Found Errors

If you get "module not found" errors after updating Xorcery:
1. Check `module-info.java` for outdated module requirements
2. Ensure all dependencies in `pom.xml` are compatible
3. Clean and rebuild: `mvn clean install`

### Neo4j Database Errors

If Neo4j fails to start:
- Check disk space
- Ensure no other Neo4j instance is running
- Delete the database directory (usually in `home/data/databases/`)

### EventStore Connection Issues

When using EventStore:
```bash
# Ensure EventStore is running
docker-compose up -d xe-eventstore

# Check logs
docker-compose logs xe-eventstore

# Access UI at http://localhost:2113
```

## Documentation

### Xorcery Framework
- **GitHub:** https://github.com/Cantara/xorcery
- **Main Repository:** For latest updates and documentation

### Blog Posts (Getting Started Guides in docs/)
Several PDF guides are available in the `docs/getting-started/` directory:
- Xorcery is born - Build high-performance microservices
- Xorcery Examples - Forum Application
- Xorcery Samples - Greeter
- Xorcery implements Reactive Streams
- Xorcery uses Jetty & Jersey and mTLS implementation and JWT support

### Related Technologies
- **Jersey:** https://eclipse-ee4j.github.io/jersey/
- **Jetty:** https://eclipse.dev/jetty/
- **Project Reactor:** https://projectreactor.io/
- **Neo4j:** https://neo4j.com/docs/
- **HK2:** https://javaee.github.io/hk2/

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow existing code style
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass: `mvn clean test`
- Keep examples simple and focused

## Version Compatibility

This examples repository uses:
- **Xorcery:** 0.132.5
- **Java:** 21+
- **Maven:** 3.8+

### Recent Updates

- Removed deprecated `ServiceResourceObjects` API
- Updated to Xorcery 0.166.9
- Migrated to new service discovery mechanisms
- Updated module dependencies

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

- **Issues:** https://github.com/Cantara/xorcery-examples/issues
- **Discussions:** https://github.com/Cantara/xorcery-examples/discussions
- **Xorcery Core:** https://github.com/Cantara/xorcery

---

**Built with ❤️ by the Cantara team**
