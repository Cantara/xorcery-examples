open module xorcery.examples.persistentsubscriber.test {
    exports com.exoreaction.xorcery.examples.persistentsubscriber.test;

    requires xorcery.reactivestreams.persistentsubscriber;

    requires xorcery.reactivestreams.server;
    requires xorcery.reactivestreams.server.extra;
    requires xorcery.jetty.server;
    requires xorcery.configuration;
    requires xorcery.junit;

    requires jakarta.inject;
    requires com.fasterxml.jackson.databind;
    requires org.apache.logging.log4j;
    requires org.glassfish.hk2.api;
    requires org.glassfish.hk2.runlevel;

}