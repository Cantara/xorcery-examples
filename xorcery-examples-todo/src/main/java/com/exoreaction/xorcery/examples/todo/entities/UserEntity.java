package com.exoreaction.xorcery.examples.todo.entities;


import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.domainevents.helpers.entity.Entity;
import com.exoreaction.xorcery.domainevents.helpers.entity.EntitySnapshot;
import com.exoreaction.xorcery.domainevents.helpers.entity.annotation.Create;

import static com.exoreaction.xorcery.domainevents.api.JsonDomainEvent.event;

public class UserEntity
        extends Entity<UserEntity.UserSnapshot>
        implements DomainModel {

    @Create
    public record Signup(String id, String email, String password)
            implements Command {
    }

/*
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
*/

    public static class UserSnapshot
            implements EntitySnapshot {
        UserStatus status;
    }

    public void handle(Signup command) {
        add(event("Created", Entity.User)
                .created(Entity.User, command.id())
                .updatedAttribute(User.status, DomainModel.UserStatus.activated)
                .build());
        add(event("Updated", User.email)
                .updated(Entity.User, command.id())
                .updatedAttribute(User.email, command.email())
                .build());
        add(event("Updated", User.password)
                .updated(Entity.User, command.id())
                .updatedAttribute(User.password, command.password().hashCode())
                .build());
    }
}
