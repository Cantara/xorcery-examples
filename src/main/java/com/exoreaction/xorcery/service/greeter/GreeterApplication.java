package com.exoreaction.xorcery.service.greeter;

import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.conductor.helpers.ClientSubscriberGroupListener;
import com.exoreaction.xorcery.service.domainevents.api.DomainEventMetadata;
import com.exoreaction.xorcery.service.domainevents.api.DomainEventPublisher;
import com.exoreaction.xorcery.service.domainevents.api.entity.DomainEvents;
import com.exoreaction.xorcery.service.greeter.commands.UpdateGreeting;
import com.exoreaction.xorcery.service.greeter.domainevents.UpdatedGreeting;
import com.exoreaction.xorcery.service.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.service.neo4j.client.GraphResult;
import com.exoreaction.xorcery.service.neo4jprojections.api.Neo4jProjectionRels;
import com.exoreaction.xorcery.service.neo4jprojections.api.WaitForProjectionCommit;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreams;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;
import org.neo4j.internal.helpers.collection.MapUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Service
@Named(GreeterApplication.SERVICE_TYPE)
public class GreeterApplication {

    public static final String SERVICE_TYPE = "greeter";

    private final DomainEventPublisher domainEventPublisher;
    private final DomainEventMetadata domainEventMetadata;
    private final GraphDatabase graphDatabase;
    private final WaitForProjectionCommit waitForProjectionCommit;

    @Inject
    public GreeterApplication(Topic<ServiceResourceObject> registryTopic,
                              DomainEventPublisher domainEventPublisher,
                              Configuration configuration,
                              GraphDatabase graphDatabase,
                              ServiceLocator serviceLocator,
                              ReactiveStreams reactiveStreams) {

        ServiceResourceObject sro = new ServiceResourceObject.Builder(() -> configuration, SERVICE_TYPE)
                .version("1.0.0")
                .attribute("domain", "greeter")
                .api("greeter", "api/greeter")
                .build();

        this.domainEventPublisher = domainEventPublisher;
        this.graphDatabase = graphDatabase;
        this.domainEventMetadata = new DomainEventMetadata(new Metadata.Builder()
                .add("domain", "greeter")
                .build());

        waitForProjectionCommit = new WaitForProjectionCommit("greeter");

        ServiceLocatorUtilities.addOneConstant(serviceLocator, new ClientSubscriberGroupListener(sro.getServiceIdentifier(),
                cfg -> waitForProjectionCommit,
                WaitForProjectionCommit.class,
                Neo4jProjectionRels.neo4jprojectioncommits.name(),
                reactiveStreams));

        registryTopic.publish(sro);
    }

    // Reads
    public CompletionStage<String> get(String name) {

        return graphDatabase.execute("MATCH (greeter:Greeter {id:$id}) RETURN greeter.greeting as greeting",
                        new MapUtil.MapBuilder<String, Object>().entry("id", "greeter").create(), 30)
                .thenApply(r ->
                {
                    try (GraphResult result = r) {
                        return result.getResult().stream().findFirst().map(m -> m.get("greeting").toString()).orElse("Hello World");
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                });
    }

    // Writes
    public CompletionStage<Metadata> handle(Record command) {
        Metadata.Builder metadata = new DomainEventMetadata.Builder(new Metadata.Builder().add(domainEventMetadata.context()))
                .timestamp(System.currentTimeMillis()).builder();

        try {
            DomainEvents domainEvents = (DomainEvents) getClass().getDeclaredMethod("handle", command.getClass()).invoke(this, command);

            Metadata md = metadata.add("commandType", command.getClass().getName()).build();
            domainEventPublisher.publish(md, domainEvents);
            return waitForProjectionCommit.waitForTimestamp(md.getLong("timestamp").orElseThrow()).thenApply(WithMetadata::metadata);
        } catch (Throwable e) {
            return CompletableFuture.failedStage(e);
        }
    }

    private DomainEvents handle(UpdateGreeting updateGreeting) {
        return DomainEvents.of(new UpdatedGreeting(updateGreeting.newGreeting()));
    }
}
