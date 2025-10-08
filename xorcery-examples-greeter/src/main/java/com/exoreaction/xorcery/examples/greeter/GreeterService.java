package com.exoreaction.xorcery.examples.greeter;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.domainevents.api.MetadataEvents;
import com.exoreaction.xorcery.neo4jprojections.api.Neo4jProjections;
import com.exoreaction.xorcery.neo4jprojections.api.ProjectionStreamContext;
import com.exoreaction.xorcery.reactivestreams.api.server.ServerWebSocketStreams;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import reactor.core.Disposable;
import reactor.util.context.Context;

@Service
@Named(GreeterApplication.SERVICE_TYPE)
@RunLevel(20)
public class GreeterService
        implements PreDestroy
{
    private final Disposable subscriber;

    @Inject
    public GreeterService(Configuration configuration,
                          ServerWebSocketStreams serverWebSocketStreams,
                          Neo4jProjections neo4jProjections) {

        subscriber = serverWebSocketStreams.subscriberWithResult("projections/greeter", MetadataEvents.class, MetadataEvents.class,
                flux -> flux.transformDeferredContextual(neo4jProjections.projection())
                        .contextWrite(Context.of(ProjectionStreamContext.projectionId, "greeter")));

        // Note: The ServiceResourceObjects registration has been removed in Xorcery 0.132.5
        // Service metadata registration is now handled through other mechanisms such as:
        // - DNS registration (via xorcery-dns-registration)
        // - Service discovery mechanisms built into the framework
    }

    @Override
    public void preDestroy() {
        subscriber.dispose();
    }
}