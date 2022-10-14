package com.exoreaction.xorcery.service.forum.model;

import com.exoreaction.xorcery.service.domainevents.api.model.CommonModel;

public interface ForumModel
        extends CommonModel {

    enum Label {
        Post,
        Comment
    }

    enum Relationship {
        HAS_COMMENT
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
