/*
 * Copyright © 2022 eXOReaction AS (rickard@exoreaction.com)
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
package examples;

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
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.util.List;
import java.util.stream.IntStream;

public class SubscribePublisherWebSocketTest {

    private String clientConf = """
                            instance.id: client
                            jetty.server.enabled: false
                            reactivestreams.server.enabled: false
            """;

    private String serverConf = """
                            instance.id: server
                            jetty.client.enabled: false
                            reactivestreams.client.enabled: false
                            jetty.server.http.enabled: false
                            jetty.server.ssl.port: "{{ SYSTEM.port }}"
            """;

    private Configuration clientConfiguration;
    private Configuration serverConfiguration;
    private ServerWebSocketStreamsConfiguration websocketStreamsServerWebSocketStreamsConfiguration;
    private Context webSocketContext;

    Logger logger = LogManager.getLogger();

    @BeforeEach
    public void setup() {
        System.setProperty("port", Integer.toString(Sockets.nextFreePort()));
        clientConfiguration = new ConfigurationBuilder().addTestDefaults().addYaml(clientConf).build();
        serverConfiguration = new ConfigurationBuilder().addTestDefaults().addYaml(serverConf).build();
        websocketStreamsServerWebSocketStreamsConfiguration = ServerWebSocketStreamsConfiguration.get(serverConfiguration);
        webSocketContext = Context.of(ClientWebSocketStreamContext.serverUri.name(), websocketStreamsServerWebSocketStreamsConfiguration.getURI().resolve("numbers"));
    }

    @Test
    public void complete() throws Exception {
        // Given
        try (Xorcery server = new Xorcery(serverConfiguration)) {
            try (Xorcery client = new Xorcery(clientConfiguration)) {
                LogManager.getLogger().info(serverConfiguration);
                ServerWebSocketStreams websocketStreamsServer = server.getServiceLocator().getService(ServerWebSocketStreams.class);
                ClientWebSocketStreams websocketStreamsClientClient = client.getServiceLocator().getService(ClientWebSocketStreams.class);

                List<Integer> source = IntStream.range(0, 100).boxed().toList();
                websocketStreamsServer.publisher(
                        "numbers",
                        Integer.class,
                        Flux.fromIterable(source));

                // When
                Flux<Integer> numbers = websocketStreamsClientClient.subscribe(
                        ClientWebSocketOptions.instance(), Integer.class, MediaType.APPLICATION_JSON
                ).contextWrite(webSocketContext);
                List<Integer> result = numbers.toStream().toList();

                // Then
                Assertions.assertEquals(source, result);
            }
        }
    }

    @Test
    public void withContext()
            throws Exception {
        // Given
        try (Xorcery server = new Xorcery(serverConfiguration)) {
            try (Xorcery client = new Xorcery(clientConfiguration)) {
                LogManager.getLogger().info(clientConfiguration);
                ServerWebSocketStreams websocketStreamsServer = server.getServiceLocator().getService(ServerWebSocketStreams.class);
                ClientWebSocketStreams websocketStreamsClientClient = client.getServiceLocator().getService(ClientWebSocketStreams.class);

                // When
                Publisher<String> configPublisher = SubscribePublisherWebSocketTest::createSubscriber;
                websocketStreamsServer.publisher(
                        "numbers/{foo}",
                        String.class,
                        configPublisher);

                String config = websocketStreamsClientClient.subscribe(
                                ClientWebSocketOptions.instance(), String.class, MediaType.APPLICATION_JSON
                        )
                        .contextWrite(Context.of(
                                ClientWebSocketStreamContext.serverUri.name(), websocketStreamsServerWebSocketStreamsConfiguration.getURI().resolve("numbers/bar?param1=value1"),
                                "client", "abc"))
                        .take(1).blockFirst();

                // Then
                Assertions.assertEquals("[foo=bar, client=abc, param1=value1]", config);
            }
        }
    }

    private static void createSubscriber(Subscriber<? super String> s) {
        if (s instanceof CoreSubscriber<? super String> subscriber) {
            String val = subscriber.currentContext().stream().filter(e -> !(e.getKey().equals("request") || e.getKey().equals("response"))).toList().toString();
            s.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    subscriber.onNext(val);
                }

                @Override
                public void cancel() {
                    subscriber.onComplete();
                }
            });
        }
    }
}
