package com.exoreaction.xorcery.examples.forum.model;

import com.exoreaction.xorcery.domainevents.helpers.model.EntityModel;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record CommentModel(ObjectNode json)
        implements EntityModel {

    public String getBody() {
        return getString(ForumModel.Comment.body).orElse("");
    }
}
