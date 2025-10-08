package com.exoreaction.xorcery.examples.forum.resources.api;

import dev.xorcery.domainevents.jsonapi.resources.CommandsJsonSchemaResource;
import dev.xorcery.domainevents.jsonapi.resources.model.CommonModel;
import com.exoreaction.xorcery.examples.forum.contexts.PostsContext;
import com.exoreaction.xorcery.examples.forum.entities.CommentEntity;
import com.exoreaction.xorcery.examples.forum.entities.PostEntity;
import com.exoreaction.xorcery.examples.forum.model.ForumModel;
import dev.xorcery.hyperschema.Link;
import dev.xorcery.jaxrs.server.resources.BaseResource;
import dev.xorcery.jsonapi.Links;
import dev.xorcery.jsonapi.MediaTypes;
import dev.xorcery.jsonapi.ResourceDocument;
import dev.xorcery.jsonapi.server.resources.JsonApiResource;
import dev.xorcery.jsonapischema.ResourceDocumentSchema;
import dev.xorcery.jsonapischema.ResourceObjectSchema;
import dev.xorcery.jsonschema.JsonSchema;
import dev.xorcery.jsonschema.server.resources.JsonSchemaResource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import static dev.xorcery.jsonapi.JsonApiRels.describedby;

@Path("api/forum")
public class ForumResource extends BaseResource
        implements JsonApiResource, JsonSchemaResource, CommandsJsonSchemaResource
{

    @GET
    @Produces(MediaTypes.APPLICATION_JSON_SCHEMA)
    public JsonSchema schema() {
        return new ResourceDocumentSchema.Builder()
                .resources(postSchema(), commentSchema())
                .included(commentSchema())
                .builder()
                .links(new dev.xorcery.hyperschema.Links.Builder()
                        .link(selfLink()).link(describedbyLink(getAbsolutePath().toASCIIString()))
                        .with(commands(PostEntity.class),
                                commands(PostsContext.class),
                                commands(CommentEntity.class),
                                l -> l.link(new Link.UriTemplateBuilder("posts")
                                        .parameter("post_fields", "Post fields", "Post fields to include")
                                        .parameter("comment_fields", "Comment fields", "Comment fields to include")
                                        .parameter("entity_fields", "Entity fields", "Entity fields to include")
                                        .parameter("include", "Included relationships", "Relations to include")
                                        .parameter("sort", "Sort", "Post sort field")
                                        .parameter("skip", "Skip", "Nr of posts to skip")
                                        .parameter("limit", "Limit", "Limit nr of posts")
                                        .build()))
                        .build())
                .builder()
                .title("Forum application")
                .build();
    }

    @GET
    public ResourceDocument get() {
        return new ResourceDocument.Builder()
                .links(new Links.Builder()
                        .link(describedby, getAbsolutePathBuilder().path(".schema").build())
                        .link("posts", getUriBuilderFor(PostsResource.class)
                                .queryParam("fields[post]", "{post_fields}")
                                .queryParam("fields[comment]", "{comment_fields}")
                                .queryParam("fields[entity]", "{entity_fields}")
                                .queryParam("include", "{include}")
                                .queryParam("sort", "{sort}")
                                .queryParam("page[skip]", "{skip}")
                                .queryParam("page[limit]", "{limit}")
                                .toTemplate())
                        .link("post", getUriBuilderFor(PostResource.class).toTemplate())
                        .build())
                .build();
    }

    private ResourceObjectSchema postSchema() {
        return new ResourceObjectSchema.Builder()
                .type(ApiTypes.post)
                .relationships(relationships(ApiRelationships.Post.values()))
                .attributes(attributes(CommonModel.Entity.values()))
                .attributes(attributes(ForumModel.Post.values()))
                .with(b -> b.builder().builder().title("Post"))
                .build();
    }

    private ResourceObjectSchema commentSchema() {
        return new ResourceObjectSchema.Builder()
                .type(ApiTypes.comment)
                .attributes(attributes(CommonModel.Entity.values()))
                .attributes(attributes(ForumModel.Comment.values()))
                .with(b -> b.builder().builder().title("Comment"))
                .build();
    }

}