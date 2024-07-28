package com.exoreaction.xorcery.examples.forum.contexts;

import com.exoreaction.xorcery.domainevents.context.CommandMetadata;
import com.exoreaction.xorcery.domainevents.context.CommandResult;
import com.exoreaction.xorcery.domainevents.context.DomainContext;
import com.exoreaction.xorcery.domainevents.entity.Command;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record PostContext(ForumApplication forumApplication, PostModel postModel, Supplier<PostEntity> postEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new PostEntity.UpdatePost(postModel.getId(), postModel.getTitle(), postModel.getBody()));
    }

    @Override
    public <T extends Command> CompletableFuture<CommandResult<T>> handle(CommandMetadata metadata, T command) {
        return forumApplication.handle(postEntitySupplier.get(), CommandMetadata.Builder.aggregate("PostAggregate", postModel.getAggregateId(), metadata.context()).context(), command);
    }
}
