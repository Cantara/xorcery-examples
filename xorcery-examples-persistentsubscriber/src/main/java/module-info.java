open module xorcery.examples.persistentsubscriber {
    exports com.exoreaction.xorcery.examples.persistentsubscriber;

    // Core modules
    requires xorcery.core;
    requires xorcery.configuration.api;

    // Domain Events
    requires xorcery.domainevents.api;

    // Neo4j Projections (replaces persistent subscriber)
    requires xorcery.neo4j.projections;
    requires xorcery.neo4j.shaded;

    // Reactive Streams
    requires xorcery.reactivestreams.api;
    requires xorcery.reactivestreams.client;

    // OpenTelemetry
    requires xorcery.opentelemetry.sdk;

    // Dependency Injection
    requires org.glassfish.hk2.api;

    // Logging
    requires org.apache.logging.log4j;

    // JSON
    requires com.fasterxml.jackson.databind;
}