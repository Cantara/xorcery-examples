package com.exoreaction.xorcery.examples.todo.model;

import dev.xorcery.domainevents.jsonapi.resources.model.CommonModel;
import dev.xorcery.neo4j.client.GraphDatabase;
import dev.xorcery.neo4j.client.GraphQuery;

import java.text.MessageFormat;
import java.util.function.BiConsumer;

import static dev.xorcery.neo4j.client.WhereClauseBuilder.where;

public record Comments(GraphDatabase db) {
    private static final String COMMENTS = MessageFormat.format(
            "MATCH ({0}:{0}) WITH {0}, {0} as {1}",
            ForumModel.Label.Comment, CommonModel.Label.Entity);

    private final static BiConsumer<GraphQuery, StringBuilder> clauses = where()
            .parameter(CommonModel.Entity.id, String.class, "Comment.id=$entity_id");

    private static final String POST_COMMENTS = MessageFormat.format(
            "MATCH ({0}:{0})-[:{1}]->({2}:{2}) WITH {2}, {2} as {3}",
            ForumModel.Label.Post, ForumModel.Relationship.PostComments, ForumModel.Label.Comment, CommonModel.Label.Entity);

    private final static BiConsumer<GraphQuery, StringBuilder> byPostClauses = where()
            .parameter(CommonModel.Entity.id, String.class, "Post.id=$entity_id");

    public GraphQuery comments() {
        return db.query(COMMENTS).where(clauses);
    }

    public GraphQuery commentsByPost(String postId)
    {
        return db.query(POST_COMMENTS).where(byPostClauses).parameter(CommonModel.Entity.id, postId);
    }
}