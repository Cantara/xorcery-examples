open module xorcery.examples {
    exports com.exoreaction.xorcery.service.forum.entities;
    exports com.exoreaction.xorcery.examples;

    requires xorcery.server;
    requires xorcery.registry;
    requires xorcery.conductor.api;
    requires xorcery.conductor;
    requires xorcery.reactivestreams;
    requires xorcery.neo4j;
    requires xorcery.eventstore;
    requires xorcery.opensearch;
    requires xorcery.log4jappender;
    requires xorcery.handlebars;
    requires xorcery.domainevents;
    requires xorcery.domainevents.neo4j;
    requires xorcery.disruptor;

    requires jakarta.annotation;
    requires org.apache.logging.log4j.core;
    requires jersey.server;
    requires com.codahale.metrics;
}