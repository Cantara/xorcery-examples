package com.exoreaction.xorcery.examples.forum.entities;

import dev.xorcery.domainevents.api.DomainEvent;
import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.entity.Entity;
import dev.xorcery.domainevents.command.annotation.Create;
import dev.xorcery.domainevents.command.annotation.Update;
import dev.xorcery.domainevents.command.annotation.Delete;

import static dev.xorcery.domainevents.api.JsonDomainEvent.event;

public class CommentEntity
        extends Entity {

    @Create
    public record CreateComment(String id, String postId, String body)
            implements Command {
    }

    @Update
    public record UpdateComment(String id, String body)
            implements Command {
    }

    @Delete
    public record DeleteComment(String id)
            implements Command {
    }

    public void handle(CreateComment command) {
        add(event("addedcomment")
                .created("Comment", command.id)
                .updatedAttribute("body", command.body)
                .addedRelationship("PostComments", "Post", metadata.getAggregateId())
                .build());
    }

    public void handle(UpdateComment command) {
        // Access the snapshot through the Element API
        String currentBody = snapshot.getString("body").orElse("");

        if (currentBody.equals(command.body))
            return;

        add(event("updatedcomment")
                .updated("Comment", command.id)
                .updatedAttribute("body", command.body)
                .build());
    }

    public void handle(DeleteComment command) {
        add(event("removedcomment").deleted("Comment", command.id()));
    }
}