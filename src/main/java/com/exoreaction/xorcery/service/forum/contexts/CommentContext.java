package com.exoreaction.xorcery.service.forum.contexts;

import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.service.domainevents.api.context.DomainContext;
import com.exoreaction.xorcery.service.domainevents.api.entity.Command;
import com.exoreaction.xorcery.service.forum.ForumApplication;
import com.exoreaction.xorcery.service.forum.entities.CommentEntity;
import com.exoreaction.xorcery.service.forum.model.CommentModel;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.service.domainevents.api.DomainEventMetadata.Builder.aggregate;

public record CommentContext(ForumApplication forumApplication, CommentModel model)
        implements DomainContext {
    @Override
    public List<Command> commands() {
        return List.of(new CommentEntity.UpdateComment(model.getId(), model.getBody()),
                new CommentEntity.RemoveComment(model.getId()));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return forumApplication.handle(new CommentEntity(), aggregate("PostAggregate", model.getAggregateId(), metadata), command);
    }
}
