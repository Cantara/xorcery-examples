package com.exoreaction.xorcery.examples.forum.contexts;

import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.model.CommentModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record CommentContext(ForumApplication forumApplication, CommentModel model, Supplier<CommentEntity> commentEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(
                new CommentEntity.UpdateComment(model.getId(), ""),
                new CommentEntity.DeleteComment(model.getId())
        );
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        return forumApplication.handle(commentEntitySupplier.get(), cm.context(), command);
    }
}