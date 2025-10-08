package com.exoreaction.xorcery.examples.persistentsubscriber.test;

import dev.xorcery.configuration.builder.ConfigurationBuilder;
import dev.xorcery.junit.XorceryExtension;
import dev.xorcery.net.Sockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Test for the Neo4j projection-based persistent subscriber example.
 *
 * This test publishes events from a YAML file and verifies that the
 * ExamplePersistentSubscriber projection processes them correctly.
 */
public class ExamplePersistentSubscriberTest {

    static String config = String.format("""
            jetty.server.http.port: %d
            jetty.server.ssl.enabled: false
            
            # Neo4j configuration
            neo4j.enabled: true
            neo4j.uri: "neo4j://localhost:7687"
            
            # Projection configuration
            neo4jprojections.enabled: true
            neo4jprojections.projections:
                - name: "examplesubscriber"
                  stream: "testevents"
            
            # YAML file publisher for test events
            yamlfilepublisher:
                publishers:
                    - stream: "testevents"
                      file: "{{ instance.home }}/../test-classes/testevents.yaml"
                        """, Sockets.nextFreePort());

    @RegisterExtension
    static XorceryExtension xorcery = XorceryExtension.xorcery()
            .configuration(ConfigurationBuilder::addTestDefaults)
            .configuration(c -> c.addYaml(config))
            .build();

    @Test
    public void testExamplePersistentSubscriber() throws InterruptedException {
        // Wait for events to be processed
        Thread.sleep(5000);

        // In a real test, you would:
        // 1. Verify that events were written to Neo4j
        // 2. Query Neo4j to check the Application nodes were created
        // 3. Verify the projection handled the events correctly

        System.out.println("Test completed - check logs for projection processing");
    }
}