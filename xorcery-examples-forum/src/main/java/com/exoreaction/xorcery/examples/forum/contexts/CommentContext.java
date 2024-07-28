package com.exoreaction.xorcery.examples.forum.contexts;

import com.exoreaction.xorcery.domainevents.context.CommandMetadata;
import com.exoreaction.xorcery.domainevents.context.CommandResult;
import com.exoreaction.xorcery.domainevents.context.DomainContext;
import com.exoreaction.xorcery.domainevents.entity.Command;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.model.CommentModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.exoreaction.xorcery.domainevents.context.CommandMetadata.Builder.aggregate;

public record CommentContext(ForumApplication forumApplication, CommentModel model,
                             Supplier<CommentEntity> commentEntitySupplier)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new CommentEntity.UpdateComment(model.getId(), model.getBody()),
                new CommentEntity.RemoveComment(model.getId()));
    }

    @Override
    public <T extends Command> CompletableFuture<CommandResult<T>> handle(CommandMetadata metadata, T command) {
        return forumApplication.handle(commentEntitySupplier.get(), aggregate("PostAggregate", model.getAggregateId(), metadata.context()).context(), command);
    }
}
