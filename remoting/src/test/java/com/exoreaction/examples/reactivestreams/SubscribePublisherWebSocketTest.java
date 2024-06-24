/*
 * Copyright © 2022 eXOReaction AS (rickard@exoreaction.com, stig@lau.no)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exoreaction.examples.reactivestreams;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.configuration.builder.ConfigurationBuilder;
import com.exoreaction.xorcery.core.Xorcery;
import com.exoreaction.xorcery.net.Sockets;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketOptions;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketStreamContext;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketStreams;
import com.exoreaction.xorcery.reactivestreams.api.server.ServerWebSocketStreams;
import com.exoreaction.xorcery.reactivestreams.server.ServerWebSocketStreamsConfiguration;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;
/*
 * Test based on com.exoreaction.xorcery.reactivestreams.test.SubscribePublisherWebSocketTest
 */
public class SubscribePublisherWebSocketTest {


    private String serverConf = """
                            instance.id: server
                            jetty.client.enabled: false
                            reactivestreams.client.enabled: false
                            jetty.server.http.enabled: false
                            jetty.server.ssl.port: "{{ SYSTEM.port }}"
            """;

    private Configuration serverConfiguration;
    private ServerWebSocketStreamsConfiguration websocketStreamsServerWebSocketStreamsConfiguration;

    @BeforeEach
    public void setup() {
        System.setProperty("port", Integer.toString(Sockets.nextFreePort()));
        serverConfiguration = new ConfigurationBuilder().addTestDefaults().addYaml(serverConf).build();
        websocketStreamsServerWebSocketStreamsConfiguration = ServerWebSocketStreamsConfiguration.get(serverConfiguration);
    }

    @Test
    public void complete() throws Exception {
        // Given
        try (Xorcery server = new Xorcery(serverConfiguration)) {
            LogManager.getLogger().info(serverConfiguration);
            ServerWebSocketStreams websocketStreamsServer = server.getServiceLocator().getService(ServerWebSocketStreams.class);

            System.out.println("Server ready, starting client");
            String from = ClientWebSocketStreamContext.serverUri.name();
            URI to = websocketStreamsServerWebSocketStreamsConfiguration.getURI().resolve("persons");

            ProducerThread treeds = new ProducerThread(websocketStreamsServer, from, to);
            treeds.start();
            System.out.println("Server waiting for client");
            Thread.sleep(1000);
            List<Person> result = treeds.numberFlux.toStream().toList();
            // Then
            Assertions.assertEquals(treeds.source, result);
        }
    }
}

class ProducerThread extends Thread {

    final Configuration clientConfiguration;
    final ServerWebSocketStreams websocketStreamsServer;
    private final Context webSocketContext;
    Flux<Person> numberFlux;
    List<Person> source;


    public ProducerThread(ServerWebSocketStreams websocketStreamsServer, String from, URI to) {
        webSocketContext = Context.of(from, to);
        this.websocketStreamsServer = websocketStreamsServer;
        this.source = IntStream.range(0, 1000000).mapToObj(Person::new).toList();

        String clientConf = """
                            instance.id: client
                            jetty.server.enabled: false
                            reactivestreams.server.enabled: false
            """;
        this.clientConfiguration = new ConfigurationBuilder().addTestDefaults().addYaml(clientConf).build();
    }

    public void run() {
        try (Xorcery client = new Xorcery(clientConfiguration)) {
            ClientWebSocketStreams websocketStreamsClientClient = client.getServiceLocator().getService(ClientWebSocketStreams.class);
            websocketStreamsServer.publisher(
                    "persons",
                    Person.class,
                    Flux.fromIterable(source));

            // When
            System.out.println("Starting client");
            numberFlux = websocketStreamsClientClient.subscribe(
                    ClientWebSocketOptions.instance(), Person.class, MediaType.APPLICATION_JSON
            ).contextWrite(webSocketContext);
            System.out.println("client finished clienting");
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

record Person(int age) {}