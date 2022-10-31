package com.exoreaction.xorcery.service.forum.contexts;

import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.service.domainevents.api.context.DomainContext;
import com.exoreaction.xorcery.service.domainevents.api.entity.Command;
import com.exoreaction.xorcery.service.forum.ForumApplication;
import com.exoreaction.xorcery.service.forum.entities.PostEntity;
import com.exoreaction.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.service.domainevents.api.DomainEventMetadata.Builder.aggregateType;

public record PostsContext(ForumApplication forumApplication)
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
                result = forumApplication.handle(new PostEntity(), aggregateType("PostAggregate", metadata.copy()), new PostEntity.CreatePost(UUIDs.newId(), createPosts.title() + " " + i, createPosts.body() + " " + i));
            }
            return result;
        } else {
            return forumApplication.handle(new PostEntity(), aggregateType("PostAggregate", metadata), command);
        }
    }

    public record CreatePosts(String title, String body, int amount)
            implements Command {

    }
}
