package com.exoreaction.xorcery.service.forum.model;

import com.exoreaction.xorcery.model.EntityModel;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record CommentModel(ObjectNode json)
        implements EntityModel {

    public String getBody() {
        return getString(ForumModel.Comment.body).orElse("");
    }
}
