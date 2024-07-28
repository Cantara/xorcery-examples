package com.exoreaction.xorcery.examples.todo.resources;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@RequiresAuthentication
public class AuthenticationRequiredFilter
        implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (requestContext.getSecurityContext().getUserPrincipal() == null &&
                requestContext.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
            requestContext.abortWith(Response
                    .temporaryRedirect(requestContext.getUriInfo().getBaseUriBuilder().path("todo/signup").build())
                    .build());
        }
    }
}
