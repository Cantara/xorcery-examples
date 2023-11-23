package com.exoreaction.xorcery.examples.forum.contexts;

import com.exoreaction.xorcery.domainevents.helpers.context.DomainContext;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata.Builder.aggregateType;

public record PostsContext(ForumApplication forumApplication, Supplier<PostEntity> postEntitySupplier)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new PostEntity.CreatePost(UUIDs.newId(), "", ""), new CreatePosts("", "", 10));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        if (command instanceof CreatePosts createPosts) {
            CompletionStage<Metadata> result = null;
            for (int i = 0; i < createPosts.amount(); i++) {
                result = forumApplication.handle(postEntitySupplier.get(), aggregateType("PostAggregate", metadata.copy()), new PostEntity.CreatePost(UUIDs.newId(), createPosts.title() + " " + i, createPosts.body() + " " + i));
            }
            return result;
        } else {
            return forumApplication.handle(postEntitySupplier.get(), aggregateType("PostAggregate", metadata), command);
        }
    }

    public record CreatePosts(String title, String body, int amount)
            implements Command {
    }
}
