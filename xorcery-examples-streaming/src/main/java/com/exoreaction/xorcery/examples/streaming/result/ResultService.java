package com.exoreaction.xorcery.examples.streaming.result;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.core.Xorcery;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketOptions;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketStreamContext;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketStreams;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import reactor.core.Disposable;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service(name = "result")
@RunLevel(20)
public class ResultService
        implements PreDestroy {

    private final Disposable disposable;

    @Inject
    public ResultService(
            Configuration configuration,
            ClientWebSocketStreams clientWebSocketStreams,
            Xorcery xorcery,
            Logger logger
    ) {
        List<String> processors = configuration.getListAs("result.processors", JsonNode::asText).orElse(Collections.emptyList());
        String source = configuration.getString("result.source").orElseThrow();
        List<String> upstream = new ArrayList<>(processors);
        Collections.reverse(upstream);
        upstream.add(source);
        String serverUri = upstream.remove(0);

        logger.info("Result starting streaming from " + source);
        disposable = clientWebSocketStreams.subscribe(ClientWebSocketOptions.instance(), JsonNode.class)
                .contextWrite(Context.of(ClientWebSocketStreamContext.serverUri, serverUri, "upstream", upstream))
                .doOnTerminate(()-> CompletableFuture.runAsync(xorcery::close))
                .subscribe(json -> System.out.println(json.toPrettyString()));
    }

    @Override
    public void preDestroy() {
        disposable.dispose();
    }
}
