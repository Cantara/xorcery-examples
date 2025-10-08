package com.exoreaction.xorcery.examples.todo.model;

import dev.xorcery.domainevents.jsonapi.resources.model.EntityModel;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record CommentModel(ObjectNode json)
        implements EntityModel {

    public String getBody() {
        return getString(ForumModel.Comment.body).orElse("");
    }
}