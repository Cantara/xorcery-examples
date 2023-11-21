package com.exoreaction.xorcery.examples.forum;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.configuration.InstanceConfiguration;
import com.exoreaction.xorcery.server.api.ServiceResourceObject;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import jakarta.inject.Inject;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

@Service(name="forum")
@RunLevel(20)
public class ForumService {
    @Inject
    public ForumService(Configuration configuration, ServiceResourceObjects serviceResourceObjects) {
        serviceResourceObjects.add(new ServiceResourceObject.Builder(new InstanceConfiguration(configuration.getConfiguration("instance")), "forum")
                .version("1.0.0")
                .attribute("domain", "forum")
                .api("forum", "api/forum")
                .build());
    }
}
