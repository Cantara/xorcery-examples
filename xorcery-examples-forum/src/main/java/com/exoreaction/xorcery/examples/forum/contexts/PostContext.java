package com.exoreaction.xorcery.examples.forum.contexts;

import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.context.DomainContext;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record PostContext(ForumApplication forumApplication, PostModel model, Supplier<PostEntity> postEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new PostEntity.UpdatePost(model.getId(), "", ""));
    }

    @Override
    public CompletableFuture<CommandResult> apply(CommandMetadata cm, Command command) {
        return forumApplication.handle(postEntitySupplier.get(), cm.context(), command);
    }
}