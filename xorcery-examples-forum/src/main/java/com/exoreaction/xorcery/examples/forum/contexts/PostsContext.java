package com.exoreaction.xorcery.examples.forum.contexts;

import com.exoreaction.xorcery.domainevents.context.CommandMetadata;
import com.exoreaction.xorcery.domainevents.context.CommandResult;
import com.exoreaction.xorcery.domainevents.context.DomainContext;
import com.exoreaction.xorcery.domainevents.entity.Command;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import com.exoreaction.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.exoreaction.xorcery.domainevents.context.CommandMetadata.Builder.aggregateType;

public record PostsContext(ForumApplication forumApplication, Supplier<PostEntity> postEntitySupplier)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new PostEntity.CreatePost(UUIDs.newId(), "", ""), new CreatePosts("", "", 10));
    }

    @Override
    public <T extends Command> CompletableFuture<CommandResult<T>> handle(CommandMetadata cm, T command) {
        if (command instanceof CreatePosts createPosts) {
            CompletableFuture<CommandResult<PostEntity.CreatePost>> result = new CompletableFuture<>();
            for (int i = 0; i < createPosts.amount(); i++) {
                result = forumApplication.handle(postEntitySupplier.get(), aggregateType("PostAggregate", cm.context().copy()).context(), new PostEntity.CreatePost(UUIDs.newId(), createPosts.title() + " " + i, createPosts.body() + " " + i));
            }
            return result.thenApply(r -> new CommandResult<>(command, r.events(), r.metadata()));
        } else {
            return forumApplication.handle(postEntitySupplier.get(), aggregateType("PostAggregate", cm.context()).context(), command);
        }
    }

    public record CreatePosts(String title, String body, int amount)
            implements Command {
    }
}
