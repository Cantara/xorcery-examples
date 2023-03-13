package com.exoreaction.xorcery.examples.greeter.domainevents;

import com.exoreaction.xorcery.domainevents.api.DomainEvent;

public record UpdatedGreeting(String greeting)
        implements DomainEvent {
}
