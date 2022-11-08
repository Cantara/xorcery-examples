package com.exoreaction.xorcery.service.forum.entities;

import com.exoreaction.xorcery.service.domainevents.api.entity.Command;
import com.exoreaction.xorcery.service.domainevents.api.entity.Entity;
import com.exoreaction.xorcery.service.domainevents.api.entity.EntitySnapshot;
import com.exoreaction.xorcery.service.domainevents.api.entity.annotation.Create;
import com.exoreaction.xorcery.service.domainevents.api.entity.annotation.Delete;
import com.exoreaction.xorcery.service.domainevents.api.entity.annotation.Update;

import static com.exoreaction.xorcery.service.domainevents.api.event.JsonDomainEvent.event;

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

    public static class CommentSnapshot
            implements EntitySnapshot {
        public String body;
    }

    private CommentSnapshot snapshot = new CommentSnapshot();

    @Override
    public CommentSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    protected void setSnapshot(CommentSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void handle(AddComment command) {
        add(event("addedcomment")
                .created("Comment", command.id)
                .attribute("body", command.body)
                .addedRelationship("PostComments", "Post", metadata.getAggregateId())
                .build());
    }

    public void handle(UpdateComment command) {
        add(event("updatedcomment")
                .updated("Comment", command.id)
                .attribute("body", command.body)
                .build());
    }

    public void handle(RemoveComment command) {
        add(event("removedcomment").deleted("Comment", command.id));
    }
}
