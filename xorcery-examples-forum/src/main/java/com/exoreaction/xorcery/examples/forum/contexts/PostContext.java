package com.exoreaction.xorcery.examples.forum.contexts;

import com.exoreaction.xorcery.domainevents.helpers.context.DomainContext;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.metadata.Metadata;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static com.exoreaction.xorcery.domainevents.helpers.context.EventMetadata.Builder.aggregate;

public record PostContext(ForumApplication forumApplication, PostModel postModel, Supplier<PostEntity> postEntitySupplier)
        implements DomainContext {

    @Override
    public List<Command> commands() {
        return List.of(new PostEntity.UpdatePost(postModel.getId(), postModel.getTitle(), postModel.getBody()));
    }

    @Override
    public CompletionStage<Metadata> handle(Metadata metadata, Command command) {
        return forumApplication.handle(postEntitySupplier.get(), aggregate("PostAggregate", postModel.getId(), metadata), command);
    }
}
