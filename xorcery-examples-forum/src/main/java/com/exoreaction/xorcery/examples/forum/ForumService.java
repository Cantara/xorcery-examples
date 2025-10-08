package com.exoreaction.xorcery.examples.forum;

import dev.xorcery.configuration.Configuration;
import jakarta.inject.Inject;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

@Service(name="forum")
@RunLevel(20)
public class ForumService {

    @Inject
    public ForumService(Configuration configuration) {
        // Service initialization for the Forum example
        //
        // Note: The ServiceResourceObjects registration has been removed in Xorcery 0.132.5
        // Service metadata registration is now handled through other mechanisms such as:
        // - DNS registration (via xorcery-dns-registration)
        // - Service discovery mechanisms built into the framework
        //
        // This service class is maintained as a placeholder for any future
        // forum-specific initialization logic that may be needed.
    }
}