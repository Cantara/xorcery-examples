package com.exoreaction.xorcery.examples.greeter;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.configuration.InstanceConfiguration;
import com.exoreaction.xorcery.server.api.ServiceResourceObject;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
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

        serviceResourceObjects.add(new ServiceResourceObject.Builder(InstanceConfiguration.get(configuration), "greeter")
                .version("1.0.0")
                .attribute("domain", "greeter")
                .api("greeter", "api/greeter")
                .build());
    }
}
