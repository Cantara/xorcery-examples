package com.exoreaction.xorcery.service.soutlogger;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.exoreaction.xorcery.configuration.model.Configuration;
import com.exoreaction.xorcery.jersey.AbstractFeature;
import com.exoreaction.xorcery.server.model.ServiceResourceObject;
import com.exoreaction.xorcery.service.conductor.api.Conductor;
import com.exoreaction.xorcery.service.conductor.helpers.ClientSubscriberConductorListener;
import com.exoreaction.xorcery.service.reactivestreams.api.ReactiveStreams;
import com.exoreaction.xorcery.service.reactivestreams.api.WithMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.core.LogEvent;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import java.util.concurrent.Flow;

/**
 * @author rickardoberg
 * @since 13/04/2022
 */

@Singleton
public class SysoutLogging
        implements ContainerLifecycleListener {

    public static final String SERVICE_TYPE = "sysoutlogging";

    @Provider
    public static class Feature
            extends AbstractFeature {
        @Override
        protected String serviceType() {
            return SERVICE_TYPE;
        }

        @Override
        protected void buildResourceObject(ServiceResourceObject.Builder builder) {
            builder.websocket("sysoutlogging", "ws/sysoutlogging");
        }

        @Override
        protected void configure() {
            context.register(SysoutLogging.class, ContainerLifecycleListener.class);
        }
    }

    private final Meter meter;
    private ServiceResourceObject serviceResourceObject;

    private ReactiveStreams reactiveStreams;
    private Configuration configuration;

    @Inject
    public SysoutLogging(ReactiveStreams reactiveStreams,
                         Conductor conductor,
                         Configuration configuration,
                         MetricRegistry metricRegistry,
                         @Named(SERVICE_TYPE) ServiceResourceObject serviceResourceObject) {
        this.reactiveStreams = reactiveStreams;
        this.configuration = configuration;
        meter = metricRegistry.meter("logmeter");
        this.serviceResourceObject = serviceResourceObject;

        serviceResourceObject.getLinkByRel("sysoutlogging").ifPresent(link ->
        {
            reactiveStreams.subscriber(link.getHrefAsUri().getPath(), cfg -> new SysoutSubscriber(), SysoutSubscriber.class);
        });

        conductor.addConductorListener(new ClientSubscriberConductorListener(serviceResourceObject.serviceIdentifier(), cfg -> new SysoutSubscriber(), SysoutSubscriber.class, "logging", reactiveStreams));
    }

    @Override
    public void onStartup(Container container) {
    }

    @Override
    public void onReload(Container container) {

    }

    @Override
    public void onShutdown(Container container) {

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
