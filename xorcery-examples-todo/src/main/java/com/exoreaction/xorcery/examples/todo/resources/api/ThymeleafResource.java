package com.exoreaction.xorcery.examples.todo.resources.api;

import com.exoreaction.xorcery.jsonapi.server.resources.ResourceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.hk2.api.ServiceLocator;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.web.servlet.IServletWebApplication;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import static jakarta.ws.rs.core.MediaType.TEXT_HTML;

@Produces(TEXT_HTML)
public abstract class ThymeleafResource
    implements ResourceContext
{

//    protected static final ObjectMapper mapper = new ObjectMapper();

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private ITemplateEngine templateEngine;

    @Context
    private ContainerRequestContext containerRequestContext;

    @Context
    private HttpServletRequest httpServletRequest;

    @Context
    private HttpServletResponse httpServletResponse;

    private JakartaServletWebApplication webApplication;

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public SecurityContext getSecurityContext() {
        return containerRequestContext.getSecurityContext();
    }

    public UriInfo getUriInfo() {
        return containerRequestContext.getUriInfo();
    }

    public ContainerRequestContext getContainerRequestContext() {
        return containerRequestContext;
    }

    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public IServletWebApplication getWebApplication()
    {
        if (webApplication == null)
        {
            webApplication = JakartaServletWebApplication.buildApplication(getHttpServletRequest().getServletContext());
        }
        return webApplication;
    }

    public IServletWebExchange getWebExchange()
    {
        return ((JakartaServletWebApplication)getWebApplication()).buildExchange(httpServletRequest, httpServletResponse);
    }

    public ITemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    @OPTIONS
    public Response options() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PATCH, OPTIONS")
                .header("Access-Control-Allow-Headers", "content-type, accept, cookie, authorization")
                .build();
    }

}
