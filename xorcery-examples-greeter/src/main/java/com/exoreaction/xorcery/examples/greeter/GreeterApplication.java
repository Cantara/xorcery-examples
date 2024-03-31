package com.exoreaction.xorcery.examples.greeter;

import com.exoreaction.xorcery.domainevents.api.DomainEvent;
import com.exoreaction.xorcery.domainevents.api.DomainEventMetadata;
import com.exoreaction.xorcery.domainevents.api.JsonDomainEvent;
import com.exoreaction.xorcery.domainevents.api.MetadataEvents;
import com.exoreaction.xorcery.domainevents.helpers.context.EventMetadata;
import com.exoreaction.xorcery.domainevents.publisher.DomainEventPublisher;
import com.exoreaction.xorcery.examples.greeter.commands.UpdateGreeting;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.neo4j.client.GraphResult;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jvnet.hk2.annotations.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static java.util.Map.entry;

@Service
@Named(GreeterApplication.SERVICE_TYPE)
public class GreeterApplication {

    public static final String SERVICE_TYPE = "greeter";

    private final DomainEventPublisher domainEventPublisher;
    private final EventMetadata domainEventMetadata;
    private final GraphDatabase graphDatabase;

    @Inject
    public GreeterApplication(DomainEventPublisher domainEventPublisher,
                              GraphDatabase graphDatabase) {

        this.domainEventPublisher = domainEventPublisher;
        this.graphDatabase = graphDatabase;
        this.domainEventMetadata = new EventMetadata(new Metadata.Builder()
                .add("domain", "greeter")
                .build());
    }

    // Reads
    public CompletionStage<String> get(String name) {

        return graphDatabase.execute("MATCH (greeter:Greeter {id:$id}) RETURN greeter.greeting as greeting",
                        Map.ofEntries(entry("id", "greeter")), 30)
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
        Metadata.Builder metadata = new EventMetadata.Builder(new Metadata.Builder().add(domainEventMetadata.context()))
                .timestamp(System.currentTimeMillis()).builder();

        try {
            List<DomainEvent> events = (List<DomainEvent>) getClass().getDeclaredMethod("handle", command.getClass()).invoke(this, command);
            Metadata md = metadata.add(DomainEventMetadata.commandName.name(), command.getClass().getName()).build();
            MetadataEvents commandEvents = new MetadataEvents(md, events);
            return domainEventPublisher.publish(commandEvents);
        } catch (Throwable e) {
            return CompletableFuture.failedStage(e);
        }
    }

    private List<DomainEvent> handle(UpdateGreeting updateGreeting) {
        return Collections.singletonList(JsonDomainEvent.event("UpdatedGreeting").updated("Greeter", "greeter")
                .updatedAttribute("greeting", updateGreeting.newGreeting())
                .build());
    }
}
