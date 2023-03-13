package com.exoreaction.xorcery.examples.forum.model;

import com.exoreaction.xorcery.domainevents.helpers.model.CommonModel;

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
