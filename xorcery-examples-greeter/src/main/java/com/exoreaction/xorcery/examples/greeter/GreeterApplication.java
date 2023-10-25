package com.exoreaction.xorcery.examples.greeter;

import com.exoreaction.xorcery.domainevents.api.DomainEvents;
import com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata;
import com.exoreaction.xorcery.domainevents.publisher.DomainEventPublisher;
import com.exoreaction.xorcery.examples.greeter.commands.UpdateGreeting;
import com.exoreaction.xorcery.examples.greeter.domainevents.UpdatedGreeting;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.neo4j.client.GraphResult;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jvnet.hk2.annotations.Service;

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
    private final DomainEventMetadata domainEventMetadata;
    private final GraphDatabase graphDatabase;

    @Inject
    public GreeterApplication(DomainEventPublisher domainEventPublisher,
                              GraphDatabase graphDatabase) {

        this.domainEventPublisher = domainEventPublisher;
        this.graphDatabase = graphDatabase;
        this.domainEventMetadata = new DomainEventMetadata(new Metadata.Builder()
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
        Metadata.Builder metadata = new DomainEventMetadata.Builder(new Metadata.Builder().add(domainEventMetadata.context()))
                .timestamp(System.currentTimeMillis()).builder();

        try {
            DomainEvents domainEvents = (DomainEvents) getClass().getDeclaredMethod("handle", command.getClass()).invoke(this, command);
            Metadata md = metadata.add("commandType", command.getClass().getName()).build();
            return domainEventPublisher.publish(md, domainEvents);
        } catch (Throwable e) {
            return CompletableFuture.failedStage(e);
        }
    }

    private DomainEvents handle(UpdateGreeting updateGreeting) {
        return DomainEvents.of(new UpdatedGreeting(updateGreeting.newGreeting()));
    }
}
