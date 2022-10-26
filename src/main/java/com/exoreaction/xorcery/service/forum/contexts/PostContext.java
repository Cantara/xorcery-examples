package com.exoreaction.xorcery.service.forum.contexts;

import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.service.domainevents.api.context.DomainContext;
import com.exoreaction.xorcery.service.domainevents.api.entity.Command;
import com.exoreaction.xorcery.service.forum.ForumApplication;
import com.exoreaction.xorcery.service.forum.entities.PostEntity;
import com.exoreaction.xorcery.service.forum.model.PostModel;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.service.domainevents.api.DomainEventMetadata.Builder.aggregate;

public record PostContext(ForumApplication forumApplication, PostModel postModel)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new PostEntity.UpdatePost(postModel.getTitle(), postModel.getBody()));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return forumApplication.handle(new PostEntity(), aggregate("PostAggregate", postModel.getId(), metadata), command);
    }
}
