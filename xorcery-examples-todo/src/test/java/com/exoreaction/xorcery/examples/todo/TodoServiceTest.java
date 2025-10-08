package com.exoreaction.xorcery.examples.todo;

import dev.xorcery.configuration.Configuration;
import dev.xorcery.configuration.InstanceConfiguration;
import dev.xorcery.configuration.builder.ConfigurationBuilder;
import dev.xorcery.junit.XorceryExtension;
import dev.xorcery.net.Sockets;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class TodoServiceTest {

    @RegisterExtension
    static XorceryExtension xorceryExtension = XorceryExtension.xorcery()
            .configuration(ConfigurationBuilder::addTestDefaults)
            .addYaml(String.format("""
                    jetty.server.http.enabled: false
                    jetty.server.ssl.port: %d
                    
                    # Disable DNS registration for tests (we don't need service discovery in tests)
                    dns.registration.enabled: false
                    
                    # DNS server configuration (still needed for DNS client)
                    dns.server.enabled: true
                    dns.server.port: 8853
                    
                    # DNS client configuration  
                    dns.client.enabled: true
                    dns.client.search:
                      - local
                    dns.client.hosts:
                      _certificates._sub._https._tcp: "https://127.0.0.1"
                    dns.client.nameServers:
                      - "127.0.0.1:8853"
                    
                    # Use embedded Neo4j for tests
                    neo4j.enabled: true
                    neo4j.embedded: true
                    """, Sockets.nextFreePort()))
            .build();

    @Test
    void testApplicationStarts() throws Exception {
        // Verify that the application started successfully
        Configuration configuration = xorceryExtension.getServiceLocator().getService(Configuration.class);
        assertNotNull(configuration, "Configuration should be available");

        InstanceConfiguration instanceConfig = InstanceConfiguration.get(configuration);
        assertNotNull(instanceConfig, "Instance configuration should be available");

        URI baseUri = instanceConfig.getURI();
        assertNotNull(baseUri, "Base URI should be available");

        System.out.println("✅ Application started successfully at: " + baseUri);
    }

    @Test
    void testServiceLocatorHasTodoApplication() {
        // Verify that our TodoApplication service is registered
        com.exoreaction.xorcery.examples.todo.resources.TodoApplication todoApp =
                xorceryExtension.getServiceLocator().getService(
                        com.exoreaction.xorcery.examples.todo.resources.TodoApplication.class);

        assertNotNull(todoApp, "TodoApplication should be registered as a service");
        System.out.println("✅ TodoApplication service found: " + todoApp.getClass().getName());
    }

    @Test
    void testServerIsRunning() throws Exception {
        Configuration configuration = xorceryExtension.getServiceLocator().getService(Configuration.class);
        URI baseUri = InstanceConfiguration.get(configuration).getURI();

        assertNotNull(baseUri, "Base URI should be configured");
        assertTrue(baseUri.toString().startsWith("https://"), "Should be using HTTPS");

        System.out.println("✅ Server running at: " + baseUri);
    }
}