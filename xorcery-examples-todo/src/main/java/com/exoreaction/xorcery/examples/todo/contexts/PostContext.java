package com.exoreaction.xorcery.examples.todo.contexts;

import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.examples.todo.entities.ProjectEntity;
import com.exoreaction.xorcery.examples.todo.model.PostModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record PostContext(TodoApplication forumApplication, PostModel postModel, Supplier<ProjectEntity> postEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new ProjectEntity.UpdatePost(postModel.getId(), postModel.getTitle(), postModel.getBody()));
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        return forumApplication.handle(postEntitySupplier.get(), cm.context(), command);
    }
}