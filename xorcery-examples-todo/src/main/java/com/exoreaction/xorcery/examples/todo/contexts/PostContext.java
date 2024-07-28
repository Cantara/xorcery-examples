package com.exoreaction.xorcery.examples.todo.contexts;

import com.exoreaction.xorcery.domainevents.helpers.context.DomainContext;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.examples.todo.entities.ProjectEntity;
import com.exoreaction.xorcery.examples.todo.model.PostModel;
import com.exoreaction.xorcery.metadata.Metadata;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata.Builder.aggregate;

public record PostContext(TodoApplication forumApplication, PostModel postModel, Supplier<ProjectEntity> postEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new ProjectEntity.UpdatePost(postModel.getId(), postModel.getTitle(), postModel.getBody()));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return forumApplication.handle(postEntitySupplier.get(), aggregate("PostAggregate", postModel.getId(), metadata), command);
    }
}
