package com.exoreaction.xorcery.examples.monitor.soutmetrics;

import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.server.api.ServiceResourceObjects;
import com.exoreaction.xorcery.service.dns.client.DnsLookupService;
import com.exoreaction.xorcery.service.reactivestreams.api.ClientConfiguration;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreamsClient;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
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
@RunLevel(20)
public class SysoutMetrics
        implements PreDestroy {
    public static final String SERVICE_TYPE = "sysoutmetrics";

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private DnsLookupService dnsLookupService;
    private Logger logger = LogManager.getLogger(getClass());

    private Set<URI> subscribed = new HashSet<>();

    @Inject
    public SysoutMetrics(ServiceResourceObjects serviceResourceObjects,
                         DnsLookupService dnsLookupService,
                         ReactiveStreamsClient reactiveStreams,
                         Configuration configuration,
                         ServiceLocator serviceLocator) {
        this.dnsLookupService = dnsLookupService;

        scheduledExecutorService.scheduleAtFixedRate(()->
        {
            dnsLookupService.resolve(URI.create("_metrics")).whenComplete((metricUris,throwable) ->
            {
                if (throwable != null)
                {

                } else
                {
                    for (URI uri : metricUris) {
                        if (!subscribed.contains(uri))
                        {
                            reactiveStreams.subscribe(uri.getAuthority(), "metrics",
                                    ()->configuration.getConfiguration("metrics.publisher"),
                                    new MetricEventSubscriber(configuration.getConfiguration("metrics.subscriber" ), scheduledExecutorService),
                                    MetricEventSubscriber.class, ClientConfiguration.defaults()).whenComplete((v, t)->
                            {
                                logger.info("Metrics subscription finished", t);
                            });
                            subscribed.add(uri);
                        }
                    }
                }
            });
        }, 0, 10, TimeUnit.SECONDS);
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
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onComplete() {

        }
    }
}
