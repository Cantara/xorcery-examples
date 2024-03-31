package com.exoreaction.xorcery.examples.forum.contexts;

import com.exoreaction.xorcery.domainevents.helpers.context.DomainContext;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.util.UUIDs;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.domainevents.helpers.context.EventMetadata.Builder.aggregate;

public record PostCommentsContext(ForumApplication forumApplication, PostModel postModel,
                                  java.util.function.Supplier<CommentEntity> commentEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new CommentEntity.AddComment(UUIDs.newId(), ""));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return forumApplication.handle(commentEntitySupplier.get(), aggregate("PostAggregate", postModel.getAggregateId(), metadata), command);
    }
}
