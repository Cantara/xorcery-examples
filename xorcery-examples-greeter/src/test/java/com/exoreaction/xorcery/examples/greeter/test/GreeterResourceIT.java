package com.exoreaction.xorcery.examples.greeter.test;

import com.exoreaction.xorcery.core.Xorcery;
import com.exoreaction.xorcery.junit.XorceryExtension;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.Fields;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class GreeterResourceIT {

    @RegisterExtension
    XorceryExtension xorceryExtension = XorceryExtension.xorcery().build();

    @Test
    void updateGreeting() throws Exception {

        Client client = xorceryExtension.getXorcery().getServiceLocator().getService(ClientBuilder.class).build();
        {
            String content = client.target("http://localhost:8889/api/greeter").request().get().readEntity(String.class);
            System.out.println(content);
        }

        {
            String content = client.target("http://localhost:8889/api/greeter").request().put(Entity.form(new Form().param("greeting", "HelloWorld!"))).readEntity(String.class);
            System.out.println(content);
        }
    }
}