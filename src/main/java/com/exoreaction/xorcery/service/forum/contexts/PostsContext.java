package com.exoreaction.xorcery.service.forum.contexts;

import com.exoreaction.xorcery.service.domainevents.api.aggregate.Command;
import com.exoreaction.xorcery.service.domainevents.api.context.DomainContext;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.service.forum.ForumApplication;
import com.exoreaction.xorcery.service.forum.resources.aggregates.PostAggregate;

import java.util.List;
import java.util.concurrent.CompletionStage;

public record PostsContext(ForumApplication forumApplication)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new PostAggregate.CreatePost("", ""), new CreatePosts("", "", 10));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        if (command instanceof CreatePosts createPosts)
        {
            CompletionStage<Metadata> result = null;
            for (int i = 0; i < createPosts.amount(); i++) {
                result = forumApplication.handle(new PostAggregate(), new Metadata(metadata.metadata().deepCopy()), new PostAggregate.CreatePost(createPosts.title()+" "+i, createPosts.body()+" "+i));
            }
            return result;
        } else
        {

            return forumApplication.handle(new PostAggregate(), metadata, command);
        }
    }

    public record CreatePosts(String title, String body, int amount)
        implements Command
    {

    }
}
