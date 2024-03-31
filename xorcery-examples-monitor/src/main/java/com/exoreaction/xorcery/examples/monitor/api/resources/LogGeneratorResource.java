package com.exoreaction.xorcery.examples.monitor.api.resources;

import com.exoreaction.xorcery.jaxrs.server.resources.BaseResource;
import com.exoreaction.xorcery.jsonapi.server.resources.JsonApiResource;
import com.github.jknack.handlebars.Context;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("api/loggenerator")
public class LogGeneratorResource extends BaseResource implements JsonApiResource{

    @Inject
    public LogGeneratorResource() {
    }

    @GET
    public Context get() {
        Logger logger = LogManager.getLogger(getClass());

        for (int i = 0; i < 10000000; i++) {
            logger.info("Count "+i);
        }

        return Context.newContext("Success");
    }
}
