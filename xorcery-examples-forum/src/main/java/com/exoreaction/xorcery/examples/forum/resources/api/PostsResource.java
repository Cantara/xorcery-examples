package com.exoreaction.xorcery.examples.forum.resources.api;

import dev.xorcery.domainevents.context.CommandMetadata;
import dev.xorcery.domainevents.context.CommandResult;
import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.forum.contexts.PostsContext;
import com.exoreaction.xorcery.examples.forum.resources.ForumApiMixin;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import dev.xorcery.jaxrs.server.resources.BaseResource;
import dev.xorcery.jsonapi.Included;
import dev.xorcery.jsonapi.Links;
import dev.xorcery.jsonapi.ResourceDocument;
import dev.xorcery.jsonapi.ResourceObject;
import dev.xorcery.jsonapi.server.resources.JsonApiResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static dev.xorcery.jsonapi.MediaTypes.APPLICATION_JSON_API;

@Path("api/forum/posts")
public class PostsResource
        extends BaseResource
        implements JsonApiResource, ForumApiMixin {

    private final PostsContext context;

    @Inject
    public PostsResource(ForumApplication forumApplication) {
        context = forumApplication.posts();
    }

    @GET
    public CompletableFuture<ResourceDocument> get(@QueryParam("rel") String rel) {
        if (rel != null) {
            return commandResourceDocument(rel, null, context);
        } else {
            Links.Builder links = new Links.Builder();
            Included.Builder included = new Included.Builder();
            return posts(included, links)
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
        String aggregateId = new CommandMetadata(commandResult.metadata()).getAggregateId();
        URI location = getUriBuilderFor(PostResource.class).build(aggregateId);
        return post(aggregateId, new Included.Builder())
                .thenApply(post -> Response.created(location).links(schemaHeader()).entity(post).build())
                .toCompletableFuture();
    }
}