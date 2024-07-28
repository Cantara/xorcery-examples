package com.exoreaction.xorcery.examples.todo.resources;

import com.exoreaction.xorcery.domainevents.api.CommandEvents;
import com.exoreaction.xorcery.domainevents.api.DomainEvent;
import com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.domainevents.helpers.entity.Entity;
import com.exoreaction.xorcery.domainevents.helpers.entity.EntitySnapshot;
import com.exoreaction.xorcery.domainevents.publisher.DomainEventPublisher;
import com.exoreaction.xorcery.domainevents.snapshot.Neo4jEntitySnapshotLoader;
import com.exoreaction.xorcery.examples.todo.contexts.*;
import com.exoreaction.xorcery.examples.todo.entities.TaskEntity;
import com.exoreaction.xorcery.examples.todo.entities.ProjectEntity;
import com.exoreaction.xorcery.examples.todo.entities.UserEntity;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.neo4j.client.GraphDatabase;
import com.exoreaction.xorcery.util.UUIDs;
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
    public <T extends EntitySnapshot> CompletionStage<Metadata> handle(Entity<T> entity, DomainEventMetadata metadata, Command command) {

        try {
            DomainEventMetadata domainMetadata = getDomainEventMetadata(metadata, command);

            T snapshot;
            if (Command.isCreate(command.getClass())) {
                // Should fail
                try {
                    snapshotLoader.load(domainMetadata, command.id(), entity);
                    return CompletableFuture.failedStage(new BadRequestException("Entity already exists"));
                } catch (Exception e) {
                    // Good!
                    Class<?> snapshotClass = (Class<?>) ((ParameterizedType) entity.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                    snapshot = (T)snapshotClass.getConstructor().newInstance();
                }
            } else {
                snapshot = snapshotLoader.load(domainMetadata, command.id(), entity);
            }

            List<DomainEvent> events = entity.handle(domainMetadata, snapshot, command);

            return domainEventPublisher.publish(new CommandEvents(metadata.context(), events));
        } catch (Throwable e) {
            return CompletableFuture.failedStage(e);
        }
    }

    private DomainEventMetadata getDomainEventMetadata(DomainEventMetadata metadata, Command command) {
        DomainEventMetadata domainMetadata = new DomainEventMetadata.Builder(metadata.context())
                .timestamp(System.currentTimeMillis())
                .correlationId(UUIDs.newId())
                .domain("todo")
                .commandName(command.getClass())
                .build();
        return domainMetadata;
    }
}
