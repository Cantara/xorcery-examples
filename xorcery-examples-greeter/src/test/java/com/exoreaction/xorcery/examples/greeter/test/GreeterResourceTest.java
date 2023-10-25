package com.exoreaction.xorcery.examples.greeter.test;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.configuration.InstanceConfiguration;
import com.exoreaction.xorcery.configuration.builder.ConfigurationBuilder;
import com.exoreaction.xorcery.junit.XorceryExtension;
import com.exoreaction.xorcery.net.Sockets;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;

class GreeterResourceTest {

    @RegisterExtension
    static XorceryExtension xorceryExtension = XorceryExtension.xorcery()
            .configuration(ConfigurationBuilder::addTestDefaults)
            .addYaml(String.format("""
                    jetty.server.http.enabled: false
                    jetty.server.ssl.port: %d
                    """, Sockets.nextFreePort()))
            .build();

    @Test
    void updateGreeting() throws Exception {

        Configuration configuration = xorceryExtension.getServiceLocator().getService(Configuration.class);
        URI baseUri = InstanceConfiguration.get(configuration).getURI();
        Client client = xorceryExtension.getServiceLocator().getService(ClientBuilder.class).build();
        {
            String content = client.target(baseUri)
                    .path("/api/greeter")
                    .request()
                    .get(String.class);
            System.out.println(content);
        }

        {
            String content = client.target(baseUri)
                    .path("/api/greeter")
                    .request()
                    .post(Entity.form(new Form().param("greeting", "HelloWorld!")), String.class);
            System.out.println(content);
        }
    }
}