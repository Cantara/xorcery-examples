open module xorcery.examples.test {
    requires xorcery.examples;
    requires xorcery.server;
    requires xorcery.client;
    requires org.eclipse.jetty.client;
    requires org.junit.jupiter.api;
    requires org.hamcrest;
    requires xorcery.domainevents;
    requires xorcery.metadata;
    requires xorcery.reactivestreams.api;
    requires jmh.core;
}