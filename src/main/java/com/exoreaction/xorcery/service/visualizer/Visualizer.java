package com.exoreaction.xorcery.service.visualizer;


import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.disruptor.handlers.DefaultEventHandler;
import com.exoreaction.xorcery.jsonapi.model.Link;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.apache.logging.log4j.core.LogEvent;
import org.glassfish.hk2.api.messaging.MessageReceiver;
import org.glassfish.hk2.api.messaging.SubscribeTo;
import org.glassfish.hk2.api.messaging.Topic;
import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Named(Visualizer.SERVICE_TYPE)
public class Visualizer {

    public static final String SERVICE_TYPE = "visualizer";

    private final AtomicInteger serviceCounter = new AtomicInteger();
    public final List<ServiceEntry> services = new CopyOnWriteArrayList<>();

    private final List<ServiceConnection> connections = new CopyOnWriteArrayList<>();

    @Inject
    public Visualizer(ServiceResourceObjects serviceResourceObjects,
                      Configuration configuration) {

        serviceResourceObjects.publish(new ServiceResourceObject.Builder(() -> configuration, SERVICE_TYPE)
                .api("visualizer", "api/visualizer")
                .build());
    }

    public List<ServiceEntry> getServices() {
        return services;
    }

    public List<ServiceConnection> getConnections() {
        return connections;
    }

    private class LogEventHandler
            implements DefaultEventHandler<WithMetadata<LogEvent>> {
        @Override
        public void onEvent(WithMetadata<LogEvent> event, long sequence, boolean endOfBatch) throws Exception {
            if (event.event().getMessage().getFormat().startsWith("Connected to")) {
                String serviceId = event.event().getMarker().getName().split(":")[1];
                String toUri = event.event().getMessage().getFormattedMessage().substring("Connected to ".length());

                int fromId = getServiceById(serviceId);
                int toId = getServiceByLink(toUri);
                if (fromId != -1 && toId != -1)
                    connections.add(new ServiceConnection(fromId, toId));
            }
        }

        private int getServiceById(String serviceId) {
            for (ServiceEntry service : services) {
                if (service.resource().getServerId().equals(serviceId))
                    return service.id();
            }
            return -1;
        }

        private int getServiceByLink(String toUri) {
            for (ServiceEntry service : services) {
                for (Link link : service.resource().resourceObject().getLinks().getLinks()) {
                    if (link.getHref().equals(toUri))
                        return service.id();
                }
            }
            return -1;
        }

    }

    private void addService(ServiceResourceObject service) {
        services.add(new ServiceEntry(service, serviceCounter.incrementAndGet()));
    }

    private record ServiceEntry(ServiceResourceObject resource, int id) {
    }

    private record ServiceConnection(int from, int to) {
    }


    @MessageReceiver
    public static class ServiceSubscriber {
        private final Provider<Visualizer> visualizerProvider;

        @Inject
        public ServiceSubscriber(Provider<Visualizer> visualizerProvider) {
            this.visualizerProvider = visualizerProvider;
        }

        public void addedService(@SubscribeTo ServiceResourceObject service) {
            visualizerProvider.get().addService(service);
        }
    }
}
