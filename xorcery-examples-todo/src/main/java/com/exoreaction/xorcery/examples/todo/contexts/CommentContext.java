package com.exoreaction.xorcery.examples.todo.contexts;

import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.examples.todo.entities.TaskEntity;
import com.exoreaction.xorcery.examples.todo.model.CommentModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record CommentContext(TodoApplication forumApplication, CommentModel model,
                             Supplier<TaskEntity> commentEntitySupplier)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new TaskEntity.UpdateTask(model.getId(), model.getBody()),
                new TaskEntity.RemoveTask(model.getId()));
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        return forumApplication.handle(commentEntitySupplier.get(), cm.context(), command);
    }
}