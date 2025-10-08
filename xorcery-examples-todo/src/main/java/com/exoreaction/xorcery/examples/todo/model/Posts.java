package com.exoreaction.xorcery.examples.todo.model;

import dev.xorcery.domainevents.jsonapi.resources.model.CommonModel;
import dev.xorcery.neo4j.client.GraphDatabase;
import dev.xorcery.neo4j.client.GraphQuery;

import java.text.MessageFormat;
import java.util.function.BiConsumer;

import static dev.xorcery.neo4j.client.WhereClauseBuilder.where;

public record Posts(GraphDatabase db) {

    private static final String POSTS = MessageFormat.format(
            "MATCH ({0}:{0}) WITH {0}, {0} as {1}",
            ForumModel.Label.Post, CommonModel.Label.Entity);

    private final static BiConsumer<GraphQuery, StringBuilder> clauses = where()
            .parameter(CommonModel.Entity.id, String.class, "Post.id=$entity_id");

    public GraphQuery posts() {
        return db.query(POSTS).where(clauses);
    }
}