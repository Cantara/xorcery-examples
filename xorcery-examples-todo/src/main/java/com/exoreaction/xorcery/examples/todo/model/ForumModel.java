package com.exoreaction.xorcery.examples.todo.model;

import dev.xorcery.domainevents.jsonapi.resources.model.CommonModel;

public interface ForumModel
        extends CommonModel {

    enum Label {
        Post,
        Comment
    }

    enum Relationship {
        PostComments
    }

    enum Post {
        title,
        body,
        is_comments_enabled
    }

    enum Comment {
        body
    }
}