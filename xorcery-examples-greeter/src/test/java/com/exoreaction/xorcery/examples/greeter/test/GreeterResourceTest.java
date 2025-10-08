package com.exoreaction.xorcery.examples.greeter.test;

import dev.xorcery.configuration.Configuration;
import dev.xorcery.configuration.InstanceConfiguration;
import dev.xorcery.configuration.builder.ConfigurationBuilder;
import dev.xorcery.junit.XorceryExtension;
import dev.xorcery.net.Sockets;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            assertEquals("""
            <html lang="en">
            <head>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
                      integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
                        integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
                        crossorigin="anonymous"></script>
            
                <title>Greeter</title>
            </head>
            <body class="bg-secondary container mt-3">
            
            <div class="card">
                <div class="card-title">
                    Greeting:<span>HelloWorld!</span>
                </div>
            
                <form method="POST" action="" class="card-body">
                    <input name="greeting" type="text"/>
                    <input type="submit" value="Submit">
                </form>
            </div>
            </body>
            </html>
            """.stripIndent(), content);
        }
    }
}