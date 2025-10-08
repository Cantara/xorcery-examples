package com.exoreaction.xorcery.examples.forum.resources;

import dev.xorcery.collections.Element;
import dev.xorcery.domainevents.api.MetadataEvents;
import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.entity.Entity;
import dev.xorcery.domainevents.publisher.api.DomainEventPublisher;
import dev.xorcery.domainevents.neo4jprojection.providers.Neo4jEntitySnapshotLoader;
import com.exoreaction.xorcery.examples.forum.contexts.CommentContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostCommentsContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostsContext;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.examples.forum.model.CommentModel;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.neo4j.client.GraphDatabase;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service(name = "forum")
public class ForumApplication {
    private static final Logger logger = LogManager.getLogger(ForumApplication.class);

    private final DomainEventPublisher domainEventPublisher;
    private final Neo4jEntitySnapshotLoader snapshotLoader;

    private final Supplier<PostEntity> postEntitySupplier;
    private final Supplier<CommentEntity> commentEntitySupplier;

    @Inject
    public ForumApplication(DomainEventPublisher domainEventPublisher,
                            GraphDatabase database,
                            ServiceLocator serviceLocator
    ) {
        this.domainEventPublisher = domainEventPublisher;
        this.snapshotLoader = new Neo4jEntitySnapshotLoader(database);
        postEntitySupplier = () -> serviceLocator.createAndInitialize(PostEntity.class);
        commentEntitySupplier = () -> serviceLocator.createAndInitialize(CommentEntity.class);
    }

    public PostsContext posts() {
        return new PostsContext(this, postEntitySupplier);
    }

    public PostContext post(PostModel postModel) {
        return new PostContext(this, postModel, postEntitySupplier);
    }

    public CommentContext comment(CommentModel model) {
        return new CommentContext(this, model, commentEntitySupplier);
    }

    public PostCommentsContext postComments(PostModel postModel) {
        return new PostCommentsContext(this, postModel, commentEntitySupplier);
    }

    public <COMMAND extends Command> CompletableFuture<CommandResult> handle(Entity entity, Metadata metadata, COMMAND command) {

        try {
            CommandMetadata domainMetadata = new CommandMetadata.Builder(metadata)
                    .domain("forum")
                    .commandName(command.getClass())
                    .build();

            // Use snapshotFor to get the snapshot
            return snapshotLoader.snapshotFor(domainMetadata, command, entity)
                    .thenCompose(snapshot -> {
                        // Call entity.handle() which returns CompletableFuture<CommandResult>
                        return entity.handle(domainMetadata, snapshot.state(), command);
                    })
                    .thenCompose(result -> {
                        // Publish the events
                        return domainEventPublisher.publish(new MetadataEvents(result.metadata(), result.events()))
                                .thenApply(md -> new CommandResult(command, result.events(), md));
                    });

        } catch (Throwable e) {
            logger.error("Error handling command", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}