package com.exoreaction.xorcery.examples.todo.resources.api;

import dev.xorcery.util.Resources;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.web.servlet.IServletWebApplication;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("todo")
@Produces("*/*")
public class StaticContentResource {
    @Inject
    private ITemplateEngine templateEngine;

    @Context
    private ContainerRequestContext containerRequestContext;

    @Context
    private HttpServletRequest httpServletRequest;

    @Context
    private HttpServletResponse httpServletResponse;

    @GET
    @Path("{path:.*}")
    public Response get(@PathParam("path") final String path) {

        if (containerRequestContext.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE) &&
                containerRequestContext.getAcceptableMediaTypes().size() == 1) {
            try {
                String templatePath = "static/" + path;
                JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(httpServletRequest.getServletContext());
                IServletWebExchange webExchange = webApplication.buildExchange(httpServletRequest, httpServletResponse);
                String content = templateEngine.process(templatePath, new WebContext(webExchange));
                return Response.ok().entity(content).build();
            } catch (TemplateInputException ex) {
                throw new NotFoundException(ex);
            } catch (Exception ex) {
                throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, ex);
            }
        } else {
            String resourcePath = "WEB-INF/static/" + path;
            return Resources.getResource(resourcePath).map(url ->
            {
                try {
                    return Response.ok().entity(url.openStream()).build();
                } catch (IOException e) {
                    throw new NotFoundException();
                }
            }).orElseGet(() -> Response.status(NOT_FOUND).build());
        }
    }
}
