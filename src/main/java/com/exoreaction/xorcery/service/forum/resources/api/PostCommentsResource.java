package com.exoreaction.xorcery.service.forum.resources.api;

import com.exoreaction.xorcery.jsonapi.server.resources.JsonApiResource;
import com.exoreaction.xorcery.service.domainevents.api.entity.Command;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.jsonapi.model.Included;
import com.exoreaction.xorcery.jsonapi.model.Links;
import com.exoreaction.xorcery.jsonapi.model.ResourceDocument;
import com.exoreaction.xorcery.jsonapi.model.ResourceObject;
import com.exoreaction.xorcery.service.domainevents.api.entity.Entity;
import com.exoreaction.xorcery.service.forum.ForumApplication;
import com.exoreaction.xorcery.service.forum.contexts.PostCommentsContext;
import com.exoreaction.xorcery.service.forum.model.PostModel;
import com.exoreaction.xorcery.service.forum.resources.ForumApiMixin;
import com.exoreaction.xorcery.service.forum.resources.entities.CommentEntity;
import com.exoreaction.xorcery.service.forum.resources.entities.PostEntity;
import com.exoreaction.xorcery.service.neo4j.client.GraphQuery;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.jsonapi.MediaTypes.APPLICATION_JSON_API;
import static com.exoreaction.xorcery.service.domainevents.api.DomainEventMetadata.Builder.aggregateId;

@Path("api/forum/posts/{id}/comments")
public class PostCommentsResource
        extends JsonApiResource
        implements ForumApiMixin {

    private PostCommentsContext context;
    private PostModel post;

    @Inject
    public void bind(ForumApplication forumApplication) {
        GraphQuery graphQuery = postByIdQuery(getFirstPathParameter("id"));
        post = graphQuery
                .first(toModel(PostModel::new, graphQuery.getResults()))
                .toCompletableFuture()
                .join();
        context = forumApplication.postComments(post);
    }

    @GET
    public CompletionStage<ResourceDocument> get(@QueryParam("rel") String rel) {
        if (rel != null) {
            return commandResourceDocument(rel, null, context);
        } else {
            Links.Builder links = new Links.Builder();
            Included.Builder included = new Included.Builder();
            return postComments(post.getId(), included, pagination(links))
                    .thenApply(ros -> new ResourceDocument.Builder()
                            .data(ros)
                            .included(included.build())
                            .links(links.with(commands(getRequestUriBuilder(), context), schemaLink()))
                            .build());
        }
    }

    @POST
    @Consumes({"application/x-www-form-urlencoded", APPLICATION_JSON_API})
    public CompletionStage<Response> post(ResourceObject resourceObject) {
        return execute(resourceObject, context, metadata());
    }

    @Override
    public CompletionStage<Response> ok(Metadata metadata, Command command) {

        if (command instanceof CommentEntity.AddComment addComment) {
            return comment(addComment.id(), new Included.Builder())
                    .thenApply(resource -> Response.created(resource.getLinks().getSelf().orElseThrow().getHrefAsUri())
                            .links(schemaHeader()).entity(resource).build());
        } else
            throw new NotFoundException();
    }
}
