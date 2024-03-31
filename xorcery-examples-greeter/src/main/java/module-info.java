open module xorcery.examples.greeter {
    exports com.exoreaction.xorcery.examples.greeter.commands;
    exports com.exoreaction.xorcery.examples.greeter.resources.api;
    exports com.exoreaction.xorcery.examples.greeter;

    requires xorcery.jsonapi.server;
    requires xorcery.metadata;
    requires xorcery.handlebars;

    requires jakarta.ws.rs;
    requires jakarta.inject;
    requires handlebars;
    requires org.apache.logging.log4j;
    requires xorcery.domainevents.api;
    requires xorcery.domainevents.publisher;
    requires xorcery.neo4j.embedded;
    requires xorcery.neo4j.projections;
    requires xorcery.reactivestreams.api;
    requires org.glassfish.hk2.api;
    requires xorcery.neo4j.shaded;
    requires xorcery.service.api;
    requires org.glassfish.hk2.runlevel;
    requires info.picocli;
    requires xorcery.runner;
    requires xorcery.jaxrs.server;
}