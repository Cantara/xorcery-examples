package com.exoreaction.xorcery.examples.todo.entities;


import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.domainevents.helpers.entity.Entity;
import com.exoreaction.xorcery.domainevents.helpers.entity.EntitySnapshot;
import com.exoreaction.xorcery.domainevents.helpers.entity.annotation.Create;
import com.exoreaction.xorcery.domainevents.helpers.entity.annotation.Delete;
import com.exoreaction.xorcery.domainevents.helpers.entity.annotation.Update;

import static com.exoreaction.xorcery.domainevents.api.JsonDomainEvent.event;

public class TaskEntity
        extends Entity<TaskEntity.TaskSnapshot>
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

    public static class TaskSnapshot
            implements EntitySnapshot {
        public String description;
    }

    public void handle(AddTask command) {
        add(event("Created",Entity.Task)
                .created(Entity.Task, command.id)
                .updatedAttribute(Task.description, command.description)
                .addedRelationship("PostComments", "Post", metadata.getAggregateId())
                .build());
        add(event("Added",Entity.Task)
                .updated(Entity.Project, command.projectId())
                .build());
    }

    public void handle(UpdateTask command) {
        if (snapshot.description.equals(command.description))
            return;

        add(event("UpdatedTask")
                .updated("Task", command.id)
                .updatedAttribute("description", command.description)
                .build());
    }

    public void handle(RemoveTask command) {
        add(event("removedcomment").deleted("Comment", command.id));
    }
}
