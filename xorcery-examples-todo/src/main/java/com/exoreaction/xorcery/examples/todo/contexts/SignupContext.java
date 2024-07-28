package com.exoreaction.xorcery.examples.todo.contexts;

import com.exoreaction.xorcery.domainevents.helpers.context.DomainContext;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.todo.entities.UserEntity;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata.Builder.aggregate;

public class SignupContext
    implements DomainContext
{
    private final TodoApplication todoApplication;
    private final Supplier<UserEntity> userEntitySupplier;

    public SignupContext(TodoApplication todoApplication, Supplier<UserEntity> userEntitySupplier) {

        this.todoApplication = todoApplication;
        this.userEntitySupplier = userEntitySupplier;
    }

    @Override
    public List<Command> commands() {
        return List.of(new UserEntity.Signup(UUIDs.newId(), "", ""));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return  todoApplication.handle(userEntitySupplier.get(), aggregate("UserAggregate", command.id(), metadata), command);
    }
}
