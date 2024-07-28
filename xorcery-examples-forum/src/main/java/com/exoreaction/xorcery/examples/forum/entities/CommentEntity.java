package com.exoreaction.xorcery.examples.forum.entities;

import com.exoreaction.xorcery.domainevents.api.JsonDomainEvent;
import com.exoreaction.xorcery.domainevents.entity.Command;
import com.exoreaction.xorcery.domainevents.entity.Entity;
import com.exoreaction.xorcery.domainevents.entity.annotation.Create;
import com.exoreaction.xorcery.domainevents.entity.annotation.Delete;
import com.exoreaction.xorcery.domainevents.entity.annotation.Update;

import static com.exoreaction.xorcery.domainevents.api.JsonDomainEvent.event;

public class CommentEntity
        extends Entity<CommentEntity.CommentSnapshot> {

    @Create
    public record AddComment(String id, String body)
            implements Command {
    }

    @Update
    public record UpdateComment(String id, String body)
            implements Command {
    }

    @Delete
    public record RemoveComment(String id)
            implements Command {
    }

    public static class CommentSnapshot {
        public String body;
    }

    public void handle(AddComment command) {
        add(event("addedcomment")
                .created("Comment", command.id)
                .updatedAttribute("body", command.body)
                .addedRelationship("PostComments", "Post", metadata.getAggregateId())
                .build());
    }

    public void handle(UpdateComment command) {
        if (snapshot.body.equals(command.body))
            return;

        add(event("updatedcomment")
                .updated("Comment", command.id)
                .updatedAttribute("body", command.body)
                .build());
    }

    public void handle(RemoveComment command) {
        add(event("removedcomment").deleted("Comment", command.id));
    }
}
