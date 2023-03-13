package com.exoreaction.xorcery.examples.greeter;

import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

@Service
@Named(GreeterApplication.SERVICE_TYPE)
@RunLevel(20)
public class GreeterService {

    @Inject
    public GreeterService(ServiceResourceObjects serviceResourceObjects,
                          Configuration configuration) {

        serviceResourceObjects.add(new ServiceResourceObject.Builder(() -> configuration, "greeter")
                .version("1.0.0")
                .attribute("domain", "greeter")
                .api("greeter", "api/greeter")
                .build());
    }
}
