package com.exoreaction.xorcery.examples.forum.resources.api;

import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.forum.contexts.PostCommentsContext;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApiMixin;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import dev.xorcery.jaxrs.server.resources.BaseResource;
import dev.xorcery.jsonapi.Included;
import dev.xorcery.jsonapi.Links;
import dev.xorcery.jsonapi.ResourceDocument;
import dev.xorcery.jsonapi.ResourceObject;
import dev.xorcery.jsonapi.server.resources.JsonApiResource;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.neo4j.client.GraphQuery;
import dev.xorcery.neo4j.client.RowModel;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletableFuture;

import static dev.xorcery.jsonapi.MediaTypes.APPLICATION_JSON_API;

@Path("api/forum/posts/{id}/comments")
public class PostCommentsResource
        extends BaseResource
        implements JsonApiResource, ForumApiMixin {

    private PostCommentsContext context;
    private PostModel post;

    @Inject
    public void bind(ForumApplication forumApplication) {
        GraphQuery graphQuery = postByIdQuery(getFirstPathParameter("id"));
        post = graphQuery
                .first(RowModel.toModel(PostModel::new, graphQuery.getResults()))
                .toCompletableFuture()
                .join().orElseThrow();
        context = forumApplication.postComments(post);
    }

    @GET
    public CompletableFuture<ResourceDocument> get(@QueryParam("rel") String rel) {
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
                            .build())
                    .toCompletableFuture();
        }
    }

    @POST
    @Consumes({"application/x-www-form-urlencoded", APPLICATION_JSON_API})
    public CompletableFuture<Response> post(ResourceObject resourceObject) {
        return execute(resourceObject, context, metadata());
    }

    @Override
    public CompletableFuture<Response> ok(CommandResult commandResult) {
        if (commandResult.command() instanceof CommentEntity.CreateComment addComment) {
            return comment(addComment.id(), new Included.Builder())
                    .thenApply(resource -> Response.created(resource.orElseThrow().getLinks().getSelf().orElseThrow().getHrefAsUri())
                            .links(schemaHeader()).entity(resource).build())
                    .toCompletableFuture();
        } else
            throw new NotFoundException();
    }
}