package examples;

import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.configuration.builder.ConfigurationBuilder;
import com.exoreaction.xorcery.core.Xorcery;
import com.exoreaction.xorcery.net.Sockets;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketStreamContext;
import com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketStreams;
import com.exoreaction.xorcery.reactivestreams.api.server.ServerWebSocketStreams;
import com.exoreaction.xorcery.reactivestreams.server.ServerWebSocketStreamsConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.stream.IntStream;

import static com.exoreaction.xorcery.reactivestreams.api.client.ClientWebSocketOptions.instance;

public class StigsTest {


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
                            jetty.server.websockets.maxBinaryMessageSize: 1024
            """;

    private Configuration clientConfiguration;
    private Configuration serverConfiguration;
    private ServerWebSocketStreamsConfiguration websocketStreamsServerWebSocketStreamsConfiguration;
    private ContextView webSocketContext;

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
                LogManager.getLogger().info(clientConfiguration);
                ServerWebSocketStreams websocketStreamsServer = server.getServiceLocator().getService(ServerWebSocketStreams.class);
                ClientWebSocketStreams websocketStreamsClientClient = client.getServiceLocator().getService(ClientWebSocketStreams.class);

                websocketStreamsServer.subscriberWithResult(
                        "numbers",
                        Integer.class,
                        Integer.class,
                        upstream -> upstream.map(v -> v));

                // When
                List<Integer> source = IntStream.range(0, 100).boxed().toList();
                List<Integer> result = Flux.fromIterable(source)
                        .transform(websocketStreamsClientClient.publishWithResult(instance(), Integer.class, Integer.class))
                        .contextWrite(webSocketContext)
                        .toStream().toList();

                // Then
                Assertions.assertEquals(source, result);
            }
        }
    }

}
