package com.exoreaction.xorcery.examples.forum.contexts;

import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import dev.xorcery.domainevents.command.Command;
import dev.xorcery.util.UUIDs;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static dev.xorcery.domainevents.context.CommandMetadata.Builder.aggregateType;

public record PostCommentsContext(ForumApplication forumApplication, PostModel postModel, Supplier<CommentEntity> commentEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new CommentEntity.CreateComment(UUIDs.newId(), postModel.getId(), ""));
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        return forumApplication.handle(commentEntitySupplier.get(), aggregateType("CommentAggregate", cm.context()).context(), command);
    }
}