package com.exoreaction.xorcery.examples.persistentsubscriber.test;

import com.exoreaction.xorcery.configuration.builder.ConfigurationBuilder;
import com.exoreaction.xorcery.junit.XorceryExtension;
import com.exoreaction.xorcery.net.Sockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ExamplePersistentSubscriberTest {

    static String config = String.format("""
            jetty.server.http.port: %d
            jetty.server.ssl.enabled: false
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
        // Xorcery will start persistent subscribers server and run the example subscriber, along with publisher of test events
        Thread.sleep(30000);
    }
}
