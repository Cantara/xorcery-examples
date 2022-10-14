open module xorcery.examples {
    exports com.exoreaction.xorcery.service.forum.resources.aggregates;
    exports com.exoreaction.xorcery.service.forum.resources.events;
    exports com.exoreaction.xorcery.examples;

    requires transitive jakarta.annotation;

    requires transitive xorcery.server;
    requires transitive xorcery.registry;
    requires transitive xorcery.conductor.api;
    requires transitive xorcery.conductor;
    requires transitive xorcery.reactivestreams;
    requires transitive xorcery.neo4j;
    requires transitive xorcery.eventstore;
    requires transitive xorcery.opensearch;
    requires transitive xorcery.log4jappender;
    requires transitive xorcery.handlebars;
    requires xorcery.domainevents;
    requires xorcery.disruptor;
    requires org.apache.logging.log4j.core;
    requires jersey.server;
    requires com.codahale.metrics;
}