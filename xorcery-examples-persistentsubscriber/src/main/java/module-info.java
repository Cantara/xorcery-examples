module xorcery.examples.persistentsubscriber {
    exports com.exoreaction.xorcery.examples.persistentsubscriber;

    // Module not available in Xorcery 0.166.9
    // requires xorcery.reactivestreams.persistentsubscriber;

    requires xorcery.core;
    requires xorcery.reactivestreams.api;
    requires xorcery.reactivestreams.client;
    requires org.glassfish.hk2.api;
    requires org.apache.logging.log4j;
}