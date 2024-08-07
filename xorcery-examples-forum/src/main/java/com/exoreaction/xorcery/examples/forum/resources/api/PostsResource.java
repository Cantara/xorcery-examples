package com.exoreaction.xorcery.examples.forum.resources.api;

import com.exoreaction.xorcery.domainevents.context.CommandMetadata;
import com.exoreaction.xorcery.domainevents.context.CommandResult;
import com.exoreaction.xorcery.domainevents.entity.Command;
import com.exoreaction.xorcery.examples.forum.contexts.PostsContext;
import com.exoreaction.xorcery.examples.forum.resources.ForumApiMixin;
import com.exoreaction.xorcery.examples.forum.resources.ForumApplication;
import com.exoreaction.xorcery.jaxrs.server.resources.BaseResource;
import com.exoreaction.xorcery.jsonapi.Included;
import com.exoreaction.xorcery.jsonapi.Links;
import com.exoreaction.xorcery.jsonapi.ResourceDocument;
import com.exoreaction.xorcery.jsonapi.ResourceObject;
import com.exoreaction.xorcery.jsonapi.server.resources.JsonApiResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.jsonapi.MediaTypes.APPLICATION_JSON_API;

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
    public CompletionStage<ResourceDocument> get(@QueryParam("rel") String rel) {
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
                            .build());
        }
    }

    @POST
    @Consumes({"application/x-www-form-urlencoded", APPLICATION_JSON_API})
    public CompletionStage<Response> post(ResourceObject resourceObject) {
        return execute(resourceObject, context, metadata());
    }

    @Override
    public <T extends Command> CompletionStage<Response> ok(CommandResult<T> commandResult) {
        String aggregateId = new CommandMetadata(commandResult.metadata()).getAggregateId();
        URI location = getUriBuilderFor(PostResource.class).build(aggregateId);
        return post(aggregateId, new Included.Builder())
                .thenApply(post -> Response.created(location).links(schemaHeader()).entity(post).build());
    }
}
