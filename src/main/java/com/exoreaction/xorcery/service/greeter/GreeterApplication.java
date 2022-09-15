package com.exoreaction.xorcery.service.greeter;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.jaxrs.AbstractFeature;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.conductor.api.Conductor;
import com.exoreaction.xorcery.service.domainevents.api.DomainEventMetadata;
import com.exoreaction.xorcery.service.domainevents.api.DomainEventPublisher;
import com.exoreaction.xorcery.service.domainevents.api.aggregate.DomainEvents;
import com.exoreaction.xorcery.service.greeter.commands.UpdateGreeting;
import com.exoreaction.xorcery.service.greeter.domainevents.UpdatedGreeting;
import com.exoreaction.xorcery.service.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.service.neo4j.client.GraphResult;
import com.exoreaction.xorcery.service.neo4jprojections.api.Neo4jProjectionRels;
import com.exoreaction.xorcery.service.neo4jprojections.api.WaitForProjectionCommit;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreams;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import com.exoreaction.xorcery.service.reactivestreams.helper.ClientSubscriberConductorListener;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.spi.Contract;
import org.neo4j.internal.helpers.collection.MapUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Singleton
@Contract
public class GreeterApplication {

    public static final String SERVICE_TYPE = "greeter";

    @Provider
    public static class Feature
            extends AbstractFeature {

        @Override
        protected String serviceType() {
            return SERVICE_TYPE;
        }

        @Override
        protected void buildResourceObject(ServiceResourceObject.Builder builder) {
            builder
                    .version("1.0.0")
                    .attribute("domain", "greeter")
                    .api("greeter", "api/greeter");
        }

        @Override
        protected void configure() {
            context.register(GreeterApplication.class, GreeterApplication.class);
        }
    }

    private DomainEventPublisher domainEventPublisher;
    private final DomainEventMetadata domainEventMetadata;
    private Configuration configuration;
    private GraphDatabase graphDatabase;
    private final WaitForProjectionCommit waitForProjectionCommit;

    @Inject
    public GreeterApplication(DomainEventPublisher domainEventPublisher,
                              Configuration configuration,
                              GraphDatabase graphDatabase,
                              Conductor conductor,
                              ReactiveStreams reactiveStreams,
                              @Named(SERVICE_TYPE) ServiceResourceObject sro) {
        this.domainEventPublisher = domainEventPublisher;
        this.configuration = configuration;
        this.graphDatabase = graphDatabase;
        this.domainEventMetadata = new DomainEventMetadata(new Metadata.Builder()
                .add("domain", "greeter")
                .build());

        waitForProjectionCommit = new WaitForProjectionCommit("forum");
        conductor.addConductorListener(new ClientSubscriberConductorListener(sro.serviceIdentifier(),
                cfg -> waitForProjectionCommit,
                WaitForProjectionCommit.class,
                Neo4jProjectionRels.neo4jprojectioncommits.name(),
                reactiveStreams));
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
        Metadata.Builder metadata = new DomainEventMetadata.Builder(new Metadata.Builder().add(domainEventMetadata.metadata()))
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
