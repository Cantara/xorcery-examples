package com.exoreaction.xorcery.examples.todo.contexts;

import com.exoreaction.xorcery.domainevents.helpers.context.DomainContext;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.examples.todo.entities.TaskEntity;
import com.exoreaction.xorcery.examples.todo.model.CommentModel;
import com.exoreaction.xorcery.metadata.Metadata;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata.Builder.aggregate;

public record CommentContext(TodoApplication forumApplication, CommentModel model,
                             Supplier<TaskEntity> commentEntitySupplier)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new TaskEntity.UpdateTask(model.getId(), model.getBody()),
                new TaskEntity.RemoveTask(model.getId()));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return forumApplication.handle(commentEntitySupplier.get(), aggregate("PostAggregate", model.getAggregateId(), metadata), command);
    }
}
