package com.exoreaction.xorcery.examples.todo.contexts;

import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.examples.todo.entities.ProjectEntity;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static dev.xorcery.domainevents.context.CommandMetadata.Builder.aggregateType;

public record PostsContext(TodoApplication forumApplication, Supplier<ProjectEntity> postEntitySupplier)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new ProjectEntity.CreatePost(UUIDs.newId(), "", ""), new CreatePosts("", "", 10));
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        if (command instanceof CreatePosts createPosts) {
            CompletableFuture<CommandResult> result = new CompletableFuture<>();
            for (int i = 0; i < createPosts.amount(); i++) {
                result = forumApplication.handle(postEntitySupplier.get(), aggregateType("PostAggregate", cm.context().copy()).context(), new ProjectEntity.CreatePost(UUIDs.newId(), createPosts.title() + " " + i, createPosts.body() + " " + i));
            }
            return result;
        } else {
            return forumApplication.handle(postEntitySupplier.get(), aggregateType("PostAggregate", cm.context()).context(), command);
        }
    }

    public record CreatePosts(String title, String body, int amount)
            implements Command {
    }
}