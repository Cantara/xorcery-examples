package com.exoreaction.xorcery.examples.monitor.soutlogger;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.exoreaction.xorcery.configuration.Configuration;
import com.exoreaction.xorcery.configuration.InstanceConfiguration;
import com.exoreaction.xorcery.reactivestreams.api.WithMetadata;
import com.exoreaction.xorcery.reactivestreams.api.server.ReactiveStreamsServer;
import com.exoreaction.xorcery.server.api.ServiceResourceObject;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.core.LogEvent;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author rickardoberg
 * @since 13/04/2022
 */

@Service
@Named(SysoutLogging.SERVICE_TYPE)
@RunLevel(20)
public class SysoutLogging {

    public static final String SERVICE_TYPE = "sysoutlogging";

    @Inject
    public SysoutLogging(ServiceResourceObjects serviceResourceObjects,
                         ReactiveStreamsServer reactiveStreamsServer,
                         Configuration configuration,
                         MetricRegistry metricRegistry) {
        reactiveStreamsServer.subscriber("logging", cfg -> new SysoutSubscriber(metricRegistry.meter("logmeter")), SysoutSubscriber.class);
        serviceResourceObjects.add(new ServiceResourceObject.Builder(InstanceConfiguration.get(configuration), SERVICE_TYPE)
                .subscriber("logging")
                .build());
    }

    private static class SysoutSubscriber
            implements Subscriber<WithMetadata<LogEvent>> {
        private Subscription subscription;
        private final Meter logmeter;

        public SysoutSubscriber(Meter logmeter) {

            this.logmeter = logmeter;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(WithMetadata<LogEvent> item) {
            System.out.println("Log:" + item.event().toString() + ":" + item.metadata().metadata().toString());
            subscription.request(1);
            logmeter.mark();
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onComplete() {

        }
    }
}
