package com.exoreaction.xorcery.service.greeter.domainevents;

import com.exoreaction.xorcery.service.domainevents.api.entity.DomainEvent;

public record UpdatedGreeting(String greeting)
    implements DomainEvent
{
}
