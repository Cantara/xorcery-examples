package com.exoreaction.xorcery.examples.todo;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.configuration.InstanceConfiguration;
import com.exoreaction.xorcery.server.api.ServiceResourceObject;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.util.Resources;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service(name="todo")
@RunLevel(20)
public class TodoService {
    @Inject
    public TodoService(
            Configuration configuration,
            ServiceResourceObjects serviceResourceObjects,
            Provider<ServletContextHandler> ctxProvider) throws URISyntaxException {

        serviceResourceObjects.add(new ServiceResourceObject.Builder(new InstanceConfiguration(configuration.getConfiguration("instance")), "todo")
                .version("1.0.0")
                .attribute("domain", "todo")
                .api("self", "todo")
                .api("api", "api/todo")
                .build());
    }
}
