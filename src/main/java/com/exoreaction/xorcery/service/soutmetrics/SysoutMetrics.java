package com.exoreaction.xorcery.service.soutmetrics;

import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.core.TopicSubscribers;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.conductor.helpers.ClientSubscriberGroupListener;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreams;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author rickardoberg
 * @since 13/04/2022
 */

@Service
@Named(SysoutMetrics.SERVICE_TYPE)
public class SysoutMetrics
        implements PreDestroy {
    public static final String SERVICE_TYPE = "sysoutmetrics";

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Inject
    public SysoutMetrics(ServiceResourceObjects serviceResourceObjects,
                         ReactiveStreams reactiveStreams,
                         Configuration configuration,
                         ServiceLocator serviceLocator) {
        ServiceResourceObject sro = new ServiceResourceObject.Builder(() -> configuration, SERVICE_TYPE)
                .websocket("sysoutmetrics", "ws/sysoutmetrics")
                .build();

        sro.getLinkByRel("sysoutmetrics").ifPresent(link ->
        {
            reactiveStreams.subscriber(link.getHrefAsUri().getPath(), cfg -> new MetricEventSubscriber(cfg, scheduledExecutorService), MetricEventSubscriber.class);
        });

        TopicSubscribers.addSubscriber(serviceLocator, new ClientSubscriberGroupListener(sro.getServiceIdentifier(), cfg -> new MetricEventSubscriber(cfg, scheduledExecutorService), MetricEventSubscriber.class, "metrics", reactiveStreams));

        serviceResourceObjects.publish(sro);
    }


    @Override
    public void preDestroy() {
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
