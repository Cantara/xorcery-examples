# xorcery-examples

## Purpose
Collection of example applications demonstrating various features and capabilities of the Xorcery framework. Includes progressively complex examples from basic greeter to full event-sourced streaming applications.

## Tech Stack
- Language: Java 21+
- Framework: Xorcery 0.166.x, HK2, Jetty 12, Jersey
- Build: Maven (multi-module POM)
- Key dependencies: Xorcery core and extensions, Kurrent (EventStore)

## Architecture
Multi-module project with four example applications:
1. **Greeter Example** - Basic Xorcery application with REST endpoint
2. **Forum Example** - Event-sourced forum with CQRS patterns
3. **Streaming Example** - Reactive streaming with Kurrent/EventStore
4. **Persistent Subscriber Example** - Durable event subscriptions

Each example includes Docker Compose setup for required infrastructure (EventStore, etc.).

## Key Entry Points
- Individual example modules in the project
- `docker-compose.yml` files for infrastructure dependencies
- Each example's `README.md` for specific instructions

## Development
```bash
# Build all examples
mvn clean install

# Run specific example
cd xorcery-examples-greeter
mvn exec:java

# Start infrastructure
docker-compose up -d
```

## Domain Context
Xorcery framework learning and reference. Demonstrates how to build applications using Xorcery's reactive, event-sourced patterns. Starting point for developers adopting the Xorcery framework.
