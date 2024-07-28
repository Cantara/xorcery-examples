package com.exoreaction.xorcery.examples.forum.contexts;

import com.exoreaction.xorcery.domainevents.context.CommandMetadata;
import com.exoreaction.xorcery.domainevents.context.CommandResult;
import com.exoreaction.xorcery.domainevents.context.DomainContext;
import com.exoreaction.xorcery.domainevents.entity.Command;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import com.exoreaction.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record PostCommentsContext(ForumApplication forumApplication, PostModel postModel,
                                  java.util.function.Supplier<CommentEntity> commentEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new CommentEntity.AddComment(UUIDs.newId(), ""));
    }

    @Override
    public <T extends Command> CompletableFuture<CommandResult<T>> handle(CommandMetadata metadata, T command) {
        return forumApplication.handle(commentEntitySupplier.get(), CommandMetadata.Builder.aggregate("PostAggregate", postModel.getAggregateId(), metadata.context()).context(), command);
    }
}
