package com.exoreaction.xorcery.examples.forum.resources;

import com.exoreaction.xorcery.domainevents.api.DomainEvent;
import com.exoreaction.xorcery.domainevents.api.MetadataEvents;
import com.exoreaction.xorcery.domainevents.context.CommandMetadata;
import com.exoreaction.xorcery.domainevents.context.CommandResult;
import com.exoreaction.xorcery.domainevents.entity.Command;
import com.exoreaction.xorcery.domainevents.entity.Entity;
import com.exoreaction.xorcery.domainevents.publisher.DomainEventPublisher;
import com.exoreaction.xorcery.domainevents.snapshot.Neo4jEntitySnapshotLoader;
import com.exoreaction.xorcery.examples.forum.contexts.CommentContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostCommentsContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostsContext;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.examples.forum.model.CommentModel;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.neo4j.client.GraphDatabase;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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

    public <SNAPSHOT, COMMAND extends Command> CompletableFuture<CommandResult<COMMAND>> handle(Entity<SNAPSHOT> entity, Metadata metadata, COMMAND command) {

        try {
            CommandMetadata domainMetadata = new CommandMetadata.Builder(metadata)
                    .domain("forum")
                    .commandName(command.getClass())
                    .build();

            SNAPSHOT snapshot;

            if (Command.isCreate(command.getClass())) {
                // Should fail
                try {
                    snapshotLoader.load(domainMetadata, command.id(), entity);
                    return CompletableFuture.failedFuture(new BadRequestException("Entity already exists"));
                } catch (Exception e) {
                    // Good!
                    Class<SNAPSHOT> snapshotClass = (Class<SNAPSHOT>) ((ParameterizedType) entity.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                    snapshot = snapshotClass.getConstructor().newInstance();
                }

            } else {
                snapshot = snapshotLoader.load(domainMetadata, command.id(), entity);
            }

            return entity.handle(domainMetadata, snapshot, command)
                    .thenCompose(result -> domainEventPublisher.publish(new MetadataEvents(result.metadata(), result.events()))
                            .thenApply(md -> new CommandResult<>(command, result.events(), md)));
        } catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
