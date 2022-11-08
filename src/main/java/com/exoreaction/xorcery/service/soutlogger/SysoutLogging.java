package com.exoreaction.xorcery.service.soutlogger;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.core.TopicSubscribers;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.conductor.helpers.ClientSubscriberGroupListener;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreams;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.core.LogEvent;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.Flow;

/**
 * @author rickardoberg
 * @since 13/04/2022
 */

@Service
@Named(SysoutLogging.SERVICE_TYPE)
public class SysoutLogging {

    public static final String SERVICE_TYPE = "sysoutlogging";

    private final Meter meter;

    @Inject
    public SysoutLogging(ServiceResourceObjects serviceResourceObjects, ReactiveStreams reactiveStreams,
                         ServiceLocator serviceLocator,
                         Configuration configuration,
                         MetricRegistry metricRegistry) {
        ServiceResourceObject sro = new ServiceResourceObject.Builder(() -> configuration, SERVICE_TYPE)
                .websocket("sysoutlogging", "ws/sysoutlogging")
                .build();
        meter = metricRegistry.meter("logmeter");

        sro.getLinkByRel("sysoutlogging").ifPresent(link ->
        {
            reactiveStreams.subscriber(link.getHrefAsUri().getPath(), cfg -> new SysoutSubscriber(), SysoutSubscriber.class);
        });

        TopicSubscribers.addSubscriber(serviceLocator, new ClientSubscriberGroupListener(sro.getServiceIdentifier(), cfg -> new SysoutSubscriber(), SysoutSubscriber.class, "logging", reactiveStreams));

        serviceResourceObjects.publish(sro);
    }

    private static class SysoutSubscriber
            implements Flow.Subscriber<WithMetadata<LogEvent>> {
        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(WithMetadata<LogEvent> item) {
            System.out.println("Log:" + item.event().toString() + ":" + item.metadata().metadata().toString());
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
