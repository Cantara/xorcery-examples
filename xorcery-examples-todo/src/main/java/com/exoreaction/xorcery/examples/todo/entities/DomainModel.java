package com.exoreaction.xorcery.examples.todo.entities;

public interface DomainModel {

    enum Entity
    {
        User,
        Project,
        Task
    }

    enum User
    {
        status,
        email,
        password,

        Projects
    }

    enum UserStatus
    {
        activated,
        deactivated
    }

    enum Project
    {
        name,

        Tasks
    }

    enum Task
    {
        description,
        status
    }

    enum TaskStatus
    {
        open,
        closed,

        Assignee
    }
}
