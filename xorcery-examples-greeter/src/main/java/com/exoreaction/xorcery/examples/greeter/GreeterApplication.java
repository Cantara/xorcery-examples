package com.exoreaction.xorcery.examples.greeter;

import dev.xorcery.domainevents.api.*;
import dev.xorcery.domainevents.publisher.api.DomainEventPublisher;
import com.exoreaction.xorcery.examples.greeter.commands.UpdateGreeting;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.neo4j.client.GraphDatabase;
import dev.xorcery.neo4j.client.GraphResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static java.util.Map.entry;

@Service
@Singleton
public class GreeterApplication {

    public static final String SERVICE_TYPE = "greeter";

    private final DomainEventPublisher domainEventPublisher;
    private final Metadata domainEventMetadata;
    private final GraphDatabase graphDatabase;

    @Inject
    public GreeterApplication(DomainEventPublisher domainEventPublisher,
                              GraphDatabase graphDatabase) {

        this.domainEventPublisher = domainEventPublisher;
        this.graphDatabase = graphDatabase;
        this.domainEventMetadata = new Metadata.Builder()
                .add("domain", "greeter")
                .build();
    }

    // Reads
    public CompletableFuture<String> get(String name) {

        return graphDatabase.execute("MATCH (greeter:Greeter {id:$id}) RETURN greeter.greeting as greeting",
                        Map.ofEntries(entry("id", "greeter")), 30)
                .thenApply(r ->
                {
                    try (GraphResult result = r) {
                        return result.getResult().stream().findFirst().map(m -> m.get("greeting").toString()).orElse("Hello World");
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }).toCompletableFuture().orTimeout(30, TimeUnit.SECONDS);
    }

    // Writes
    public CompletableFuture<Metadata> handle(Record command) {
        // Build metadata using the DomainEventMetadata enum fields
        Metadata metadata = new Metadata.Builder()
                .add(domainEventMetadata)
                .add(DomainEventMetadata.timestamp, System.currentTimeMillis())
                .add(DomainEventMetadata.commandName, command.getClass().getName())
                .build();

        try {
            List<DomainEvent> events = (List<DomainEvent>) getClass().getDeclaredMethod("handle", command.getClass()).invoke(this, command);
            MetadataEvents metadataEvents = new MetadataEvents(metadata, events);
            return domainEventPublisher.publish(metadataEvents);
        } catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private List<DomainEvent> handle(UpdateGreeting updateGreeting) {
        return Collections.singletonList(JsonDomainEvent.event("UpdatedGreeting").updated("Greeter", "greeter")
                .updatedAttribute("greeting", updateGreeting.newGreeting())
                .build());
    }
}