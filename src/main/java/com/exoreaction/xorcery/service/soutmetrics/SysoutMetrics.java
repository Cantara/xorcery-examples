package com.exoreaction.xorcery.service.soutmetrics;

import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.jersey.AbstractFeature;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.conductor.api.Conductor;
import com.exoreaction.xorcery.service.conductor.helpers.ClientSubscriberConductorListener;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreams;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author rickardoberg
 * @since 13/04/2022
 */

@Singleton
public class SysoutMetrics
        implements ContainerLifecycleListener {
    public static final String SERVICE_TYPE = "sysoutmetrics";

    @Provider
    public static class Feature
            extends AbstractFeature {

        @Override
        protected String serviceType() {
            return SERVICE_TYPE;
        }

        @Override
        protected void buildResourceObject(ServiceResourceObject.Builder builder) {
            builder.websocket("sysoutmetrics", "ws/sysoutmetrics");
        }

        @Override
        protected void configure() {
            context.register(SysoutMetrics.class, ContainerLifecycleListener.class);
        }
    }

    private ReactiveStreams reactiveStreams;
    private Conductor conductor;
    private ServiceResourceObject sro;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Inject
    public SysoutMetrics(ReactiveStreams reactiveStreams,
                         Conductor conductor,
                         @Named(SERVICE_TYPE) ServiceResourceObject sro) {
        this.reactiveStreams = reactiveStreams;
        this.conductor = conductor;
        this.sro = sro;

        sro.getLinkByRel("sysoutmetrics").ifPresent(link ->
        {
            reactiveStreams.subscriber(link.getHrefAsUri().getPath(), cfg -> new MetricEventSubscriber(cfg, scheduledExecutorService), MetricEventSubscriber.class);
        });

        conductor.addConductorListener(new ClientSubscriberConductorListener(sro.serviceIdentifier(), cfg -> new MetricEventSubscriber(cfg, scheduledExecutorService), MetricEventSubscriber.class, "metrics", reactiveStreams));
    }

    @Override
    public void onStartup(Container container) {
    }

    @Override
    public void onReload(Container container) {
    }

    @Override
    public void onShutdown(Container container) {
        scheduledExecutorService.shutdown();
    }

    private static class MetricEventSubscriber
            implements Flow.Subscriber<WithMetadata<ObjectNode>> {
        private final ScheduledExecutorService scheduledExecutorService;
        private Flow.Subscription subscription;
        private final long delay;

        public MetricEventSubscriber(Configuration consumerConfiguration, ScheduledExecutorService scheduledExecutorService) {

            this.scheduledExecutorService = scheduledExecutorService;
            this.delay = Duration.parse(consumerConfiguration.getString("delay").orElse("5S")).toSeconds();
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(WithMetadata<ObjectNode> item) {
            System.out.println("Metric:" + item.event().toString() + ":" + item.metadata().toString());
            scheduledExecutorService.schedule(() -> subscription.request(1), delay, TimeUnit.SECONDS);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onComplete() {

        }
    }
}
