# Xorcery Persistent Subscriber Example (Neo4j Projections)

This example demonstrates how to use Neo4j projections in Xorcery 0.166.9+ to handle domain events persistently.

## What Changed from Previous Versions

In earlier versions of Xorcery, there was a `xorcery-reactivestreams-persistentsubscriber` module. This has been replaced with a more flexible **projections-based architecture**.

**Old approach:**
- Extended `BasePersistentSubscriber`
- Generic persistent subscription

**New approach (this example):**
- Implements `Neo4jEventProjection` interface
- Event handling with Neo4j transactions
- More control over data persistence

## How It Works

1. **ExamplePersistentSubscriber** implements `Neo4jEventProjection`
2. The `write()` method receives batches of domain events with metadata
3. Events are filtered (e.g., only "CreateApplication" commands)
4. Filtered events are written to Neo4j within a transaction
5. Neo4j manages the persistence and consistency

## Running the Example

```bash
mvn clean install
java -jar target/xorcery-examples-persistentsubscriber-*.jar