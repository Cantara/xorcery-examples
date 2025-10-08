package com.exoreaction.xorcery.examples.streaming.processors;

import dev.xorcery.configuration.Configuration;
import dev.xorcery.reactivestreams.api.client.ClientWebSocketOptions;
import dev.xorcery.reactivestreams.api.client.ClientWebSocketStreamContext;
import dev.xorcery.reactivestreams.api.client.ClientWebSocketStreams;
import dev.xorcery.reactivestreams.api.server.ServerWebSocketOptions;
import dev.xorcery.reactivestreams.api.server.ServerWebSocketStreams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

public abstract class ProcessorService
        implements PreDestroy, Publisher<JsonNode> {
    private final Disposable disposable;
    private final ClientWebSocketStreams clientWebSocketStreams;
    private final Function<Flux<JsonNode>, Publisher<JsonNode>> processorFunction;
    private final String processorName;
    private final Logger logger;

    @Inject
    public ProcessorService(
            String processorName,
            Function<Flux<JsonNode>, Publisher<JsonNode>> processorFunction,
            ClientWebSocketStreams clientWebSocketStreams,
            ServerWebSocketStreams serverWebSocketStreams,
            Logger logger
    ) {
        this.processorName = processorName;
        this.logger = logger;
        this.clientWebSocketStreams = clientWebSocketStreams;
        this.processorFunction = processorFunction;
        disposable = serverWebSocketStreams.publisher(processorName, ServerWebSocketOptions.instance(), JsonNode.class, this);
        logger.info("Processor {} started", processorName);
    }

    @Override
    public void subscribe(Subscriber<? super JsonNode> subscriber) {
        if (subscriber instanceof CoreSubscriber<? super JsonNode> coreSubscriber) {
            List<String> upstream = coreSubscriber.currentContext().get("upstream");
            String serverUriString = upstream.remove(0);
            URI serverUri = URI.create(serverUriString);
            logger.info("Processor {} connecting to {}", processorName, serverUri);

            clientWebSocketStreams.subscribe(serverUri, ClientWebSocketOptions.instance(), JsonNode.class)
                    .transformDeferred(processorFunction)
                    .contextWrite(context -> Context.of(ClientWebSocketStreamContext.serverUri, serverUri, "upstream", upstream))
                    .subscribe(subscriber);
        }
    }

    @Override
    public void preDestroy() {
        disposable.dispose();
    }
}