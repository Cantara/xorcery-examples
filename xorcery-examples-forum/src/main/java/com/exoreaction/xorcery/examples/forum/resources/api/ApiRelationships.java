package com.exoreaction.xorcery.examples.forum.resources.api;

import com.exoreaction.xorcery.jsonschema.server.annotations.Cardinality;
import com.exoreaction.xorcery.jsonschema.server.annotations.RelationshipSchema;

public interface ApiRelationships {
    enum Post {
        @RelationshipSchema(
                title = "Comments",
                description = "Comments for this post",
                cardinality = Cardinality.many
        )
        comments;
    }
}
