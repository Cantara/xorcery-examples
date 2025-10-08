package com.exoreaction.xorcery.examples.forum.resources.api;

import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.forum.contexts.PostContext;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.examples.forum.resources.ForumApiMixin;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import dev.xorcery.jaxrs.server.resources.BaseResource;
import dev.xorcery.jsonapi.Included;
import dev.xorcery.jsonapi.Links;
import dev.xorcery.jsonapi.ResourceDocument;
import dev.xorcery.jsonapi.ResourceObject;
import dev.xorcery.neo4j.client.GraphQuery;
import dev.xorcery.neo4j.client.RowModel;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletableFuture;

import static dev.xorcery.jsonapi.MediaTypes.APPLICATION_JSON_API;

@Path("api/forum/posts/{id}")
public class PostResource extends BaseResource
        implements ForumApiMixin {

    private PostModel post;
    private PostContext context;

    @Inject
    public void bind(ForumApplication forumApplication) {
        GraphQuery graphQuery = postByIdQuery(getFirstPathParameter("id"));
        post = graphQuery
                .first(RowModel.toModel(PostModel::new, graphQuery.getResults()))
                .toCompletableFuture()
                .join().orElseThrow();
        context = forumApplication.post(post);
    }

    @GET
    public CompletableFuture<ResourceDocument> get(@QueryParam("rel") String rel) {
        if (rel != null) {
            return commandResourceDocument(rel, post.getId(), context);
        } else {
            Links.Builder links = new Links.Builder();
            Included.Builder included = new Included.Builder();
            return CompletableFuture.completedFuture(
                    new ResourceDocument.Builder()
                            .data(postResource(included, "").apply(post))
                            .included(included)
                            .links(links.with(schemaLink()))
                            .build());
        }
    }

    @POST
    @Consumes({"application/x-www-form-urlencoded", APPLICATION_JSON_API})
    public CompletableFuture<Response> post(ResourceObject resourceObject) {
        return execute(resourceObject, context, metadata());
    }

    @PATCH
    @Consumes({"application/x-www-form-urlencoded", APPLICATION_JSON_API})
    public CompletableFuture<Response> patch(ResourceObject resourceObject) {
        return execute(resourceObject, context, metadata());
    }

    @Override
    public CompletableFuture<Response> ok(CommandResult commandResult) {
        return post(post.getId(), new Included.Builder())
                .thenApply(resource -> Response.ok(resource).links(schemaHeader()).build())
                .toCompletableFuture();
    }
}