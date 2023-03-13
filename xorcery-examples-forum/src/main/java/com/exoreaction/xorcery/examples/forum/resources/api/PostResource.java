package com.exoreaction.xorcery.examples.forum.resources.api;

import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.forum.contexts.PostContext;
import com.exoreaction.xorcery.examples.forum.model.PostModel;
import com.exoreaction.xorcery.jsonapi.model.Included;
import com.exoreaction.xorcery.jsonapi.model.Links;
import com.exoreaction.xorcery.jsonapi.model.ResourceDocument;
import com.exoreaction.xorcery.jsonapi.model.ResourceObject;
import com.exoreaction.xorcery.jsonapi.server.resources.JsonApiResource;
import com.exoreaction.xorcery.metadata.Metadata;
import com.exoreaction.xorcery.examples.forum.ForumApplication;
import com.exoreaction.xorcery.examples.forum.resources.ForumApiMixin;
import com.exoreaction.xorcery.service.neo4j.client.GraphQuery;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.exoreaction.xorcery.jsonapi.MediaTypes.APPLICATION_JSON_API;

@Path("api/forum/posts/{id}")
public class PostResource
        extends JsonApiResource
        implements ForumApiMixin {

    private PostModel post;
    private PostContext context;

    @Inject
    public void bind(ForumApplication forumApplication) {
        GraphQuery graphQuery = postByIdQuery(getFirstPathParameter("id"));
        post = graphQuery
                .first(toModel(PostModel::new, graphQuery.getResults()))
                .toCompletableFuture()
                .join();
        context = forumApplication.post(post);
    }

    @GET
    public CompletionStage<ResourceDocument> get(@QueryParam("rel") String rel) {
        if (rel != null) {
            return commandResourceDocument(rel, post.getId(), context);
        } else {
            Links.Builder links = new Links.Builder();
            Included.Builder included = new Included.Builder();
            return CompletableFuture.completedStage(
                    new ResourceDocument.Builder()
                            .data(postResource(included, "").apply(post))
                            .included(included)
                            .links(links.with(schemaLink()))
                            .build());
        }
    }

    @POST
    @Consumes({"application/x-www-form-urlencoded", APPLICATION_JSON_API})
    public CompletionStage<Response> post(ResourceObject resourceObject) {
        return execute(resourceObject, context, metadata());
    }

    @PATCH
    @Consumes({"application/x-www-form-urlencoded", APPLICATION_JSON_API})
    public CompletionStage<Response> patch(ResourceObject resourceObject) {
        return execute(resourceObject, context, metadata());
    }

    @Override
    public CompletionStage<Response> ok(Metadata metadata, Command command) {
        return post(post.getId(), new Included.Builder())
                .thenApply(resource -> Response.ok(resource).links(schemaHeader()).build());
    }
}
