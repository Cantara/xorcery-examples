module xorcery.examples.streaming {

    exports com.exoreaction.xorcery.examples.streaming.source;
    exports com.exoreaction.xorcery.examples.streaming.processors;
    exports com.exoreaction.xorcery.examples.streaming.result;

    requires xorcery.core;
    requires xorcery.configuration.api;
    requires xorcery.reactivestreams.api;

    requires org.glassfish.hk2.api;
    requires org.glassfish.hk2.runlevel;
    requires reactor.core;
    requires jakarta.inject;

    requires org.apache.logging.log4j;
}