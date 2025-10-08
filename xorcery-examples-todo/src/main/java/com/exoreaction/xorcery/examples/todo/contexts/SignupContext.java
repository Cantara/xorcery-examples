package com.exoreaction.xorcery.examples.todo.contexts;

import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import com.exoreaction.xorcery.examples.todo.entities.UserEntity;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static dev.xorcery.domainevents.context.CommandMetadata.Builder.aggregateType;

public record SignupContext(TodoApplication todoApplication, Supplier<UserEntity> userEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new UserEntity.Signup(UUIDs.newId(), "", ""));
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        return todoApplication.handle(userEntitySupplier.get(), aggregateType("UserAggregate", cm.context()).context(), command);
    }
}