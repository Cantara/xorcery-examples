open module xorcery.examples {
    exports com.exoreaction.xorcery.examples;

    exports com.exoreaction.xorcery.service.forum;
    exports com.exoreaction.xorcery.service.forum.contexts;
    exports com.exoreaction.xorcery.service.forum.entities;
    exports com.exoreaction.xorcery.service.forum.model;
    exports com.exoreaction.xorcery.service.forum.resources;

    exports com.exoreaction.xorcery.service.greeter;
    exports com.exoreaction.xorcery.service.greeter.commands;
    exports com.exoreaction.xorcery.service.greeter.domainevents;
    exports com.exoreaction.xorcery.service.greeter.resources.api;

    requires xorcery.client;
    requires xorcery.server;
    requires xorcery.registry;
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
    requires xorcery.jsonapi.jaxrs;

    requires jakarta.annotation;
    requires org.apache.logging.log4j.core;
    requires jersey.server;
    requires com.codahale.metrics;
}