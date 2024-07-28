package com.exoreaction.xorcery.examples.streaming.processors;

import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketStreams;
import com.exoreaction.xorcery.reactivestreams.api.server.ServerWebSocketStreams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Service(name = "processor1")
@RunLevel(11)
public class Processor1Service
        extends ProcessorService {

    @Inject
    public Processor1Service(
            ClientWebSocketStreams clientWebSocketStreams,
            ServerWebSocketStreams serverWebSocketStreams,
            Logger logger
    ) {
        super("processor1", Processor1Service::transform, clientWebSocketStreams, serverWebSocketStreams, logger);
    }

    protected static Publisher<JsonNode> transform(Flux<JsonNode> from)
    {
        return from.map(Processor1Service::process);
    }

    protected static JsonNode process(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode objectNode) {
            objectNode.set("processor1", JsonNodeFactory.instance.textNode("foo"));
        }
        return jsonNode;
    }
}
