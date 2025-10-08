open module xorcery.examples.persistentsubscriber {
    exports com.exoreaction.xorcery.examples.persistentsubscriber.test;

    // Core modules
    requires xorcery.core;
    requires xorcery.configuration.api;
    requires xorcery.configuration;  // For ConfigurationBuilder in tests

    // Domain Events
    requires xorcery.domainevents.api;

    // Neo4j Projections
    requires xorcery.neo4j.projections;
    requires xorcery.neo4j.shaded;

    // Reactive Streams
    requires xorcery.reactivestreams.api;
    requires xorcery.reactivestreams.client;
    requires xorcery.reactivestreams.server;  // For tests
    requires xorcery.reactivestreams.extras;  // For tests

    // Jetty (for tests)
    requires xorcery.jetty.server;

    // OpenTelemetry
    requires xorcery.opentelemetry.sdk;

    // Dependency Injection
    requires org.glassfish.hk2.api;

    // Logging
    requires org.apache.logging.log4j;

    // JSON
    requires com.fasterxml.jackson.databind;

    // Testing
    requires xorcery.junit;
    requires org.junit.jupiter.api;
}