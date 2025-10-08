package com.exoreaction.xorcery.examples.todo.entities;

import dev.xorcery.domainevents.command.Command;
import dev.xorcery.domainevents.entity.Entity;
import dev.xorcery.domainevents.command.annotation.Create;

import static dev.xorcery.domainevents.api.JsonDomainEvent.event;

public class UserEntity
        extends Entity {

    @Create
    public record Signup(String id, String email, String password)
            implements Command {
    }

    public static class UserSnapshot {
        public String email;
        public String passwordHash;
        public String status;
    }

    public void handle(Signup command) {
        add(event("userregistered")
                .created("User", command.id)
                .updatedAttribute("email", command.email)
                .updatedAttribute("passwordHash", String.valueOf(command.password.hashCode()))
                .updatedAttribute("status", "active")
                .build());
    }
}