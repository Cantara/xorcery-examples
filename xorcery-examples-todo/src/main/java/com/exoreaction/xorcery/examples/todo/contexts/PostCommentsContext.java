package com.exoreaction.xorcery.examples.todo.contexts;

import com.exoreaction.xorcery.domainevents.helpers.context.DomainContext;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.examples.todo.entities.TaskEntity;
import com.exoreaction.xorcery.examples.todo.model.PostModel;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata.Builder.aggregate;

public record PostCommentsContext(TodoApplication forumApplication, PostModel postModel,
                                  java.util.function.Supplier<TaskEntity> commentEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new TaskEntity.AddTask(UUIDs.newId(), null, ""));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return forumApplication.handle(commentEntitySupplier.get(), aggregate("PostAggregate", postModel.getAggregateId(), metadata), command);
    }
}
