package com.exoreaction.xorcery.examples.monitor.soutlogger;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreamsServer;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.core.LogEvent;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.Flow;

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
        serviceResourceObjects.add(new ServiceResourceObject.Builder(() -> configuration, SERVICE_TYPE)
                .subscriber("logging")
                .build());
    }

    private static class SysoutSubscriber
            implements Flow.Subscriber<WithMetadata<LogEvent>> {
        private Flow.Subscription subscription;
        private final Meter logmeter;

        public SysoutSubscriber(Meter logmeter) {

            this.logmeter = logmeter;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
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
