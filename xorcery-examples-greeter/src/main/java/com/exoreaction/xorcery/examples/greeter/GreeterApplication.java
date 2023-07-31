package com.exoreaction.xorcery.examples.greeter;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.domainevents.api.DomainEvents;
import com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata;
import com.exoreaction.xorcery.domainevents.publisher.DomainEventPublisher;
import com.exoreaction.xorcery.examples.greeter.commands.UpdateGreeting;
import com.exoreaction.xorcery.examples.greeter.domainevents.UpdatedGreeting;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.neo4j.client.GraphResult;
import com.exoreaction.xorcery.neo4jprojections.api.Neo4jProjectionStreams;
import com.exoreaction.xorcery.neo4jprojections.api.WaitForProjectionCommit;
import com.exoreaction.xorcery.reactivestreams.api.WithMetadata;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientConfiguration;
import com.exoreaction.xorcery.reactivestreams.api.client.ReactiveStreamsClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
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
    public GreeterApplication(DomainEventPublisher domainEventPublisher,
                              Configuration configuration,
                              GraphDatabase graphDatabase,
                              ReactiveStreamsClient reactiveStreams) {

        this.domainEventPublisher = domainEventPublisher;
        this.graphDatabase = graphDatabase;
        this.domainEventMetadata = new DomainEventMetadata(new Metadata.Builder()
                .add("domain", "greeter")
                .build());

        waitForProjectionCommit = new WaitForProjectionCommit("greeter");

        reactiveStreams.subscribe(null, Neo4jProjectionStreams.COMMIT_PUBLISHER,
                Configuration::empty, waitForProjectionCommit, WaitForProjectionCommit.class, ClientConfiguration.defaults())
                .whenComplete((r,t)->
                {
                    if (t != null)
                        LogManager.getLogger(getClass()).error("Could not creation projection waiter", t);
                });
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
