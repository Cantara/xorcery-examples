package com.exoreaction.xorcery.examples.todo.entities;

import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.entity.Entity;
import dev.xorcery.domainevents.command.annotation.Create;
import dev.xorcery.domainevents.command.annotation.Delete;
import dev.xorcery.domainevents.command.annotation.Update;

import static dev.xorcery.domainevents.api.JsonDomainEvent.event;

public class TaskEntity
        extends Entity
        implements DomainModel {

    @Create
    public record AddTask(String id, String projectId, String description)
            implements Command {
    }

    @Update
    public record UpdateTask(String id, String description)
            implements Command {
    }

    @Delete
    public record RemoveTask(String id)
            implements Command {
    }

    public static class TaskSnapshot {
        public String description;
    }

    public void handle(AddTask command) {
        add(event("TaskCreated")
                .created("Task", command.id)
                .updatedAttribute("description", command.description)
                .addedRelationship("ProjectTasks", "Project", command.projectId)
                .build());
    }

    public void handle(UpdateTask command) {
        // Access the snapshot through the Element API
        String currentDescription = snapshot.getString("description").orElse("");

        if (currentDescription.equals(command.description))
            return;

        add(event("TaskUpdated")
                .updated("Task", command.id)
                .updatedAttribute("description", command.description)
                .build());
    }

    public void handle(RemoveTask command) {
        add(event("TaskRemoved").deleted("Task", command.id));
    }
}