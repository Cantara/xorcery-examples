package com.exoreaction.xorcery.examples.forum;

import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.configuration.model.InstanceConfiguration;
import com.exoreaction.xorcery.domainevents.api.DomainEvents;
import com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.domainevents.helpers.entity.Entity;
import com.exoreaction.xorcery.domainevents.helpers.entity.EntitySnapshot;
import com.exoreaction.xorcery.domainevents.publisher.DomainEventPublisher;
import com.exoreaction.xorcery.examples.forum.contexts.PostCommentsContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostsContext;
import com.exoreaction.xorcery.examples.forum.model.CommentModel;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.domainevents.snapshot.Neo4jEntitySnapshotLoader;
import com.exoreaction.xorcery.examples.forum.contexts.CommentContext;
import com.exoreaction.xorcery.service.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.service.neo4jprojections.api.Neo4jProjectionStreams;
import com.exoreaction.xorcery.service.neo4jprojections.api.WaitForProjectionCommit;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreamsClient;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Service
@Named(ForumApplication.SERVICE_TYPE)
@RunLevel(20)
public class ForumApplication {
    private static final Logger logger = LogManager.getLogger(ForumApplication.class);

    public static final String SERVICE_TYPE = "forum";

    private final DomainEventPublisher domainEventPublisher;
    private final Neo4jEntitySnapshotLoader snapshotLoader;
    private final WaitForProjectionCommit waitForProjectionCommit;

    @Inject
    public ForumApplication(ServiceResourceObjects serviceResourceObjects,
                            DomainEventPublisher domainEventPublisher,
                            Configuration configuration,
                            ServiceLocator serviceLocator,
                            ReactiveStreamsClient reactiveStreams,
                            GraphDatabase database
    ) {
        this.domainEventPublisher = domainEventPublisher;
        this.snapshotLoader = new Neo4jEntitySnapshotLoader(database);

        waitForProjectionCommit = new WaitForProjectionCommit("forum");
        reactiveStreams.subscribe(null, Neo4jProjectionStreams.COMMIT_PUBLISHER,
                Configuration::empty, waitForProjectionCommit, WaitForProjectionCommit.class, Configuration.empty());

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
        serviceResourceObjects.add(new ServiceResourceObject.Builder(new InstanceConfiguration(configuration.getConfiguration("instance")), SERVICE_TYPE)
                .version("1.0.0")
                .attribute("domain", "forum")
                .api("forum", "api/forum")
                .build());
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

    public <T extends EntitySnapshot> CompletionStage<Metadata> handle(Entity<T> entity, DomainEventMetadata metadata, Command command) {

        try {
            DomainEventMetadata domainMetadata = new DomainEventMetadata.Builder(metadata.context())
                    .domain("forum")
                    .commandType(command.getClass())
                    .build();

            T snapshot;

            if (Command.isCreate(command.getClass())) {
                // Should fail
                try {
                    snapshotLoader.load(domainMetadata, entity);
                    return CompletableFuture.failedStage(new BadRequestException("Entity already exists"));
                } catch (Exception e) {
                    // Good!
                    snapshot = entity.getSnapshot();
                }

            } else {
                snapshot = snapshotLoader.load(domainMetadata, entity);
            }

            DomainEvents events = entity.handle(domainMetadata, snapshot, command);

            domainEventPublisher.publish(metadata.context(), events);
            return waitForProjectionCommit.waitForTimestamp(domainMetadata.getTimestamp())
                    .orTimeout(10, TimeUnit.SECONDS).thenApply(WithMetadata::metadata);
        } catch (Throwable e) {
            return CompletableFuture.failedStage(e);
        }
    }
}
