package com.exoreaction.xorcery.service.forum.resources.entities;

import com.exoreaction.xorcery.service.domainevents.api.entity.Command;
import com.exoreaction.xorcery.service.domainevents.api.entity.Entity;
import com.exoreaction.xorcery.service.domainevents.api.entity.EntitySnapshot;
import com.exoreaction.xorcery.service.domainevents.api.entity.annotation.Create;
import com.exoreaction.xorcery.service.domainevents.api.entity.annotation.Update;

import static com.exoreaction.xorcery.service.domainevents.api.event.JsonDomainEvent.event;

public class PostEntity
        extends Entity<PostEntity.PostSnapshot> {

    @Create
    public record CreatePost(String title, String body)
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
