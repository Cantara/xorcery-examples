package com.exoreaction.xorcery.examples.todo.contexts;

import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.examples.todo.entities.TaskEntity;
import com.exoreaction.xorcery.examples.todo.model.PostModel;
import dev.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static dev.xorcery.domainevents.context.CommandMetadata.Builder.aggregateType;

public record PostCommentsContext(TodoApplication forumApplication, PostModel postModel,
                                  Supplier<TaskEntity> commentEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new TaskEntity.AddTask(UUIDs.newId(), postModel.getId(), ""));
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        return forumApplication.handle(commentEntitySupplier.get(), aggregateType("PostAggregate", cm.context()).context(), command);
    }
}