package com.exoreaction.xorcery.examples.forum.entities;

import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.domainevents.helpers.entity.Entity;
import com.exoreaction.xorcery.domainevents.helpers.entity.EntitySnapshot;
import com.exoreaction.xorcery.domainevents.helpers.entity.annotation.Create;
import com.exoreaction.xorcery.domainevents.helpers.entity.annotation.Update;

import static com.exoreaction.xorcery.domainevents.api.JsonDomainEvent.event;

public class PostEntity
        extends Entity<PostEntity.PostSnapshot> {

    @Create
    public record CreatePost(String id, String title, String body)
            implements Command {
    }

    @Update
    public record UpdatePost(String title, String body)
            implements Command {
    }

    public static class PostSnapshot
            implements EntitySnapshot {
        public String title;
    }

    private PostSnapshot snapshot = new PostSnapshot();

    @Override
    public PostSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    protected void setSnapshot(PostSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void handle(CreatePost command) {
        add(event("createdpost")
                .created("Post", metadata.getAggregateId())
                .attribute("title", command.title)
                .attribute("body", command.body)
                .build());
    }

    public void handle(UpdatePost command) {
        add(event("updatedpost")
                .updated("Post", metadata.getAggregateId())
                .attribute("title", command.title)
                .attribute("body", command.body)
                .build());
    }
}
