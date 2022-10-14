package com.exoreaction.xorcery.service.forum;

import com.exoreaction.xorcery.jersey.AbstractFeature;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.conductor.api.Conductor;
import com.exoreaction.xorcery.service.conductor.helpers.ClientSubscriberConductorListener;
import com.exoreaction.xorcery.service.domainevents.api.DomainEventMetadata;
import com.exoreaction.xorcery.service.domainevents.api.DomainEventPublisher;
import com.exoreaction.xorcery.service.domainevents.api.aggregate.Aggregate;
import com.exoreaction.xorcery.service.domainevents.api.aggregate.AggregateSnapshot;
import com.exoreaction.xorcery.service.domainevents.api.aggregate.Command;
import com.exoreaction.xorcery.service.domainevents.api.aggregate.DomainEvents;
import com.exoreaction.xorcery.service.forum.contexts.CommentContext;
import com.exoreaction.xorcery.service.forum.contexts.PostCommentsContext;
import com.exoreaction.xorcery.service.forum.contexts.PostContext;
import com.exoreaction.xorcery.service.forum.contexts.PostsContext;
import com.exoreaction.xorcery.service.forum.model.CommentModel;
import com.exoreaction.xorcery.service.forum.model.PostModel;
import com.exoreaction.xorcery.service.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.service.neo4jprojections.aggregate.Neo4jAggregateSnapshotLoader;
import com.exoreaction.xorcery.service.neo4jprojections.api.Neo4jProjectionRels;
import com.exoreaction.xorcery.service.neo4jprojections.api.WaitForProjectionCommit;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreams;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.spi.Contract;

import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Singleton
@Contract
public class ForumApplication {
    private static final Logger logger = LogManager.getLogger(ForumApplication.class);

    public static final String SERVICE_TYPE = "forum";

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
                    .attribute("domain", "forum")
                    .api("forum", "api/forum");
        }

        @Override
        protected void configure() {
            context.register(ForumApplication.class, ForumApplication.class);
        }
    }

    private final DomainEventPublisher domainEventPublisher;
    private final Neo4jAggregateSnapshotLoader snapshotLoader;
    private final WaitForProjectionCommit waitForProjectionCommit;

    @Inject
    public ForumApplication(@Named(SERVICE_TYPE) ServiceResourceObject sro,
                            DomainEventPublisher domainEventPublisher,
                            Conductor conductor,
                            ReactiveStreams reactiveStreams,
                            GraphDatabase database
    ) {
        this.domainEventPublisher = domainEventPublisher;
        this.snapshotLoader = new Neo4jAggregateSnapshotLoader(database);

        waitForProjectionCommit = new WaitForProjectionCommit("forum");
        conductor.addConductorListener(new ClientSubscriberConductorListener(sro.serviceIdentifier(),
                cfg -> waitForProjectionCommit,
                WaitForProjectionCommit.class,
                Neo4jProjectionRels.neo4jprojectioncommits.name(),
                reactiveStreams));

/*
        try {
            long now = System.currentTimeMillis();
            DomainEventMetadata domainEventMetadata = new DomainEventMetadata.Builder(new Metadata.Builder())
                    .commandType(PostAggregate.CreatePost.class)
                    .timestamp(now).build();
            domainEventPublisher.publish(domainEventMetadata.metadata(), DomainEvents.of());
            logger.info("Published startup noop events");

            CompletionStage<?> isLive = waitForProjectionCommit.waitForTimestamp(domainEventMetadata.getTimestamp());

            isLive.toCompletableFuture().get(60, TimeUnit.SECONDS);

            logger.info("Forum is live!");
        } catch (Throwable e) {
            logger.error("Could not wait for projection to start", e);
        }
*/
    }

    public PostsContext posts() {
        return new PostsContext(this);
    }

    public PostContext post(PostModel postModel) {
        return new PostContext(this, postModel);
    }

    public CommentContext comment(CommentModel model) {
        return new CommentContext(this, model);
    }

    public PostCommentsContext postComments(PostModel postModel) {
        return new PostCommentsContext(this, postModel);
    }

    public <T extends AggregateSnapshot> CompletionStage<Metadata> handle(Aggregate<T> aggregate, Metadata metadata, Command command) {

        try {
            DomainEventMetadata domainMetadata = new DomainEventMetadata.Builder(metadata.toBuilder())
                    .domain("forum")
                    .aggregateType(aggregate.getClass())
                    .commandType(command.getClass())
                    .build();

            T snapshot;

            if (Command.isCreate(command.getClass())) {
                // Should fail
                try {
                    snapshotLoader.load(domainMetadata, aggregate);
                    return CompletableFuture.failedStage(new BadRequestException("Entity already exists"));
                } catch (Exception e) {
                    // Good!
                    snapshot = aggregate.getSnapshot();
                }

            } else {
                snapshot = snapshotLoader.load(domainMetadata, aggregate);
            }

            DomainEvents events = aggregate.handle(domainMetadata.context(), snapshot, command);

            domainEventPublisher.publish(metadata, events);
            return waitForProjectionCommit.waitForTimestamp(domainMetadata.getTimestamp())
                    .orTimeout(10, TimeUnit.SECONDS).thenApply(WithMetadata::metadata);
        } catch (Throwable e) {
            return CompletableFuture.failedStage(e);
        }
    }
}
