package com.exoreaction.xorcery.examples.forum.model;

import com.exoreaction.xorcery.domainevents.jsonapi.resources.model.EntityModel;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record PostModel(ObjectNode json)
        implements EntityModel {
    public String getTitle() {
        return getString(ForumModel.Post.title).orElse("");
    }

    public String getBody() {
        return getString(ForumModel.Post.body).orElse("");
    }
}
