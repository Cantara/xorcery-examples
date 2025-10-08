package com.exoreaction.xorcery.examples.todo.entities;

import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.entity.Entity;
import dev.xorcery.domainevents.command.annotation.Create;
import dev.xorcery.domainevents.command.annotation.Update;

import static dev.xorcery.domainevents.api.JsonDomainEvent.event;

public class ProjectEntity
        extends Entity {

    @Create
    public record CreatePost(String id, String title, String body)
            implements Command {
    }

    @Update
    public record UpdatePost(String id, String title, String body)
            implements Command {
    }

    public static class PostSnapshot {
        public String title;
        public String body;
    }

    public void handle(CreatePost command) {
        add(event("createdpost")
                .created("Post", command.id)
                .updatedAttribute("title", command.title)
                .updatedAttribute("body", command.body)
                .build());
    }

    public void handle(UpdatePost command) {
        add(event("updatedpost")
                .updated("Post", command.id)
                .updatedAttribute("title", command.title)
                .updatedAttribute("body", command.body)
                .build());
    }
}