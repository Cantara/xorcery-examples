package com.exoreaction.xorcery.examples.forum.resources;

import com.exoreaction.xorcery.domainevents.api.CommandEvents;
import com.exoreaction.xorcery.domainevents.api.DomainEvent;
import com.exoreaction.xorcery.domainevents.helpers.context.DomainEventMetadata;
import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.domainevents.helpers.entity.Entity;
import com.exoreaction.xorcery.domainevents.helpers.entity.EntitySnapshot;
import com.exoreaction.xorcery.domainevents.publisher.DomainEventPublisher;
import com.exoreaction.xorcery.domainevents.snapshot.Neo4jEntitySnapshotLoader;
import com.exoreaction.xorcery.examples.forum.contexts.CommentContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostCommentsContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostContext;
import com.exoreaction.xorcery.examples.forum.contexts.PostsContext;
import com.exoreaction.xorcery.examples.forum.model.CommentModel;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.neo4j.client.GraphDatabase;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service(name="forum")
public class ForumApplication {
    private static final Logger logger = LogManager.getLogger(ForumApplication.class);

    private final DomainEventPublisher domainEventPublisher;
    private final Neo4jEntitySnapshotLoader snapshotLoader;

    @Inject
    public ForumApplication(DomainEventPublisher domainEventPublisher,
                            GraphDatabase database
    ) {
        this.domainEventPublisher = domainEventPublisher;
        this.snapshotLoader = new Neo4jEntitySnapshotLoader(database);
    }

    public PostsContext posts() {
        return new PostsContext(this);
    }

    public PostContext post(PostModel postModel) {
        return new PostContext(this, postModel);
    }

    public CommentContext comment(CommentModel model) {
        return new CommentContext(this, model);
    }

    public PostCommentsContext postComments(PostModel postModel) {
        return new PostCommentsContext(this, postModel);
    }

    public <T extends EntitySnapshot> CompletionStage<Metadata> handle(Entity<T> entity, DomainEventMetadata metadata, Command command) {

        try {
            DomainEventMetadata domainMetadata = new DomainEventMetadata.Builder(metadata.context())
                    .domain("forum")
                    .commandName(command.getClass())
                    .build();

            T snapshot;

            if (Command.isCreate(command.getClass())) {
                // Should fail
                try {
                    snapshotLoader.load(domainMetadata, entity);
                    return CompletableFuture.failedStage(new BadRequestException("Entity already exists"));
                } catch (Exception e) {
                    // Good!
                    snapshot = entity.getSnapshot();
                }

            } else {
                snapshot = snapshotLoader.load(domainMetadata, entity);
            }

            List<DomainEvent> events = entity.handle(domainMetadata, snapshot, command);

            return domainEventPublisher.publish(new CommandEvents(metadata.context(), events));
        } catch (Throwable e) {
            return CompletableFuture.failedStage(e);
        }
    }
}
