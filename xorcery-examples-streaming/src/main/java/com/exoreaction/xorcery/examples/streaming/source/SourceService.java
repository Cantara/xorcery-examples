package com.exoreaction.xorcery.examples.streaming.source;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.reactivestreams.api.server.ServerWebSocketStreams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.IntStream;

@Service(name="source")
@RunLevel(10)
public class SourceService
    implements PreDestroy
{
    private final Disposable disposable;

    @Inject
    public SourceService(Configuration configuration, ServerWebSocketStreams serverWebSocketStreams, Logger logger) {
        List<Integer> source = IntStream.range(0, 100).boxed().toList();
        Publisher<JsonNode> publisher = Flux.fromIterable(source)
                .doOnSubscribe(s -> System.out.println("Subscribe to source"))
                .map(val -> JsonNodeFactory.instance.objectNode().set("value", JsonNodeFactory.instance.numberNode(val)));
        disposable = serverWebSocketStreams.publisher("source", JsonNode.class, publisher);
        logger.info("Source started");
    }

    @Override
    public void preDestroy() {
        disposable.dispose();
    }
}
