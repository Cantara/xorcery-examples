package com.exoreaction.xorcery.examples.todo.resources;

import dev.xorcery.collections.Element;
import dev.xorcery.domainevents.api.MetadataEvents;
import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.entity.Entity;
import dev.xorcery.domainevents.publisher.api.DomainEventPublisher;
import dev.xorcery.domainevents.neo4jprojection.providers.Neo4jEntitySnapshotLoader;
import com.exoreaction.xorcery.examples.todo.contexts.*;
import com.exoreaction.xorcery.examples.todo.entities.TaskEntity;
import com.exoreaction.xorcery.examples.todo.entities.ProjectEntity;
import com.exoreaction.xorcery.examples.todo.entities.UserEntity;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.neo4j.client.GraphDatabase;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@Service(name="todo")
public class TodoApplication {
    private static final Logger logger = LogManager.getLogger(TodoApplication.class);

    private final DomainEventPublisher domainEventPublisher;
    private final Neo4jEntitySnapshotLoader snapshotLoader;

    private final Supplier<UserEntity> userEntitySupplier;
    private final Supplier<ProjectEntity> projectEntitySupplier;
    private final Supplier<TaskEntity> taskEntitySupplier;

    @Inject
    public TodoApplication(DomainEventPublisher domainEventPublisher,
                           GraphDatabase database,
                           ServiceLocator serviceLocator
    ) {
        this.domainEventPublisher = domainEventPublisher;
        this.snapshotLoader = new Neo4jEntitySnapshotLoader(database);
        userEntitySupplier = () -> serviceLocator.createAndInitialize(UserEntity.class);
        projectEntitySupplier = () -> serviceLocator.createAndInitialize(ProjectEntity.class);
        taskEntitySupplier = () -> serviceLocator.createAndInitialize(TaskEntity.class);
    }

    public SignupContext signup()
    {
        return new SignupContext(this, userEntitySupplier);
    }

    /*
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

    */
    public <COMMAND extends Command> CompletableFuture<CommandResult> handle(Entity entity, Metadata metadata, COMMAND command) {

        try {
            CommandMetadata domainMetadata = new CommandMetadata.Builder(metadata)
                    .domain("todo")
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