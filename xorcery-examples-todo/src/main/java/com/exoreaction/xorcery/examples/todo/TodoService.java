package com.exoreaction.xorcery.examples.todo;

import dev.xorcery.configuration.Configuration;
import dev.xorcery.domainevents.api.MetadataEvents;
import dev.xorcery.neo4jprojections.api.Neo4jProjections;
import dev.xorcery.neo4jprojections.api.ProjectionStreamContext;
import dev.xorcery.reactivestreams.api.server.ServerWebSocketOptions;
import dev.xorcery.reactivestreams.api.server.ServerWebSocketStreams;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import reactor.core.Disposable;
import reactor.util.context.Context;

@Service(name="todo")
@RunLevel(20)
public class TodoService
        implements PreDestroy {

    private final Disposable subscriber;

    @Inject
    public TodoService(Configuration configuration,
                       ServerWebSocketStreams serverWebSocketStreams,
                       Neo4jProjections neo4jProjections) {

        // Setup Neo4j projection subscriber for the todo domain
        subscriber = serverWebSocketStreams.subscriberWithResult(
                "projections/todo",
                ServerWebSocketOptions.instance(),
                MetadataEvents.class,
                MetadataEvents.class,
                flux -> flux.transformDeferredContextual(neo4jProjections.projection())
                        .contextWrite(Context.of(ProjectionStreamContext.projectionId, "todo")));
    }

    @Override
    public void preDestroy() {
        subscriber.dispose();
    }
}