package com.exoreaction.xorcery.examples.greeter.resources.api;

import com.exoreaction.xorcery.examples.greeter.GreeterApplication;
import com.exoreaction.xorcery.examples.greeter.commands.UpdateGreeting;
import dev.xorcery.jaxrs.server.resources.BaseResource;
import dev.xorcery.thymeleaf.resources.ThymeleafResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.thymeleaf.context.WebContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Path("api/greeter")
public class GreeterResource
        extends BaseResource
        implements ThymeleafResource
{
    private GreeterApplication application;

    @Inject
    public GreeterResource(GreeterApplication application) {
        this.application = application;
    }

    @GET
    public WebContext get() {
        return newWebContext(Map.of("greeting", application.get("greeting").join()));
    }

    @POST
    public WebContext post(@FormParam("greeting") String greetingString) {
        return application.handle(new UpdateGreeting(greetingString)).thenApply(md -> get()).orTimeout(10, TimeUnit.SECONDS).join();
    }
}