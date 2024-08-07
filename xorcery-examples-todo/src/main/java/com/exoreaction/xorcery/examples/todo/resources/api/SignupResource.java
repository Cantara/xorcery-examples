package com.exoreaction.xorcery.examples.todo.resources.api;

import com.exoreaction.xorcery.domainevents.helpers.entity.Command;
import com.exoreaction.xorcery.examples.todo.entities.UserEntity;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import com.exoreaction.xorcery.json.JsonMerger;
import com.exoreaction.xorcery.jwt.server.JwtServerConfiguration;
import com.exoreaction.xorcery.jwt.server.JwtService;
import com.exoreaction.xorcery.metadata.Metadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Path("todo/signup")
public class SignupResource
        extends ThymeleafResource {
    private final TodoApplication todoApplication;
    private final JwtService jwtService;

    @Inject
    public SignupResource(TodoApplication todoApplication,
                          JwtService jwtService) {
        this.todoApplication = todoApplication;
        this.jwtService = jwtService;
    }

    @GET
    public String get() {
        WebContext context = new WebContext(getWebExchange());
        Command command = todoApplication.signup().commands().get(0);
        context.setVariable("command", command);
        return getTemplateEngine().process("signup", context);
    }

    @POST
    public Response signup(Form form) {
        return todoApplication.signup().command(UserEntity.Signup.class).map(signup ->
        {
            ObjectNode commandJson = objectMapper().valueToTree(signup);
            ObjectNode formJson = objectMapper().valueToTree(form.asMap());
            ObjectNode combinedJson = new JsonMerger().merge(commandJson, formJson);
            try {
                return (UserEntity.Signup) objectMapper().treeToValue(combinedJson, signup.getClass());
            } catch (IOException e) {
                throw new BadRequestException(e);
            }
        }).map(signup ->
        {
            Metadata metadata = todoApplication.signup().handle(new Metadata.Builder().build(), signup).toCompletableFuture().orTimeout(30, TimeUnit.SECONDS).join();
            try {
                String token = jwtService.createJwt(signup.id());
                JwtServerConfiguration jwtServerConfiguration = jwtService.getJwtServerConfiguration();
                Date expiresAt = Date.from(Instant.now().plus(jwtServerConfiguration.getCookieDuration()));
                return Response.ok()
                        .cookie(new NewCookie.Builder(jwtServerConfiguration.getCookieName())
                                .path(jwtServerConfiguration.getCookiePath())
                                .value(token)
                                .domain(jwtServerConfiguration.getCookieDomain())
                                .expiry(expiresAt)
                                .build()).build();
            } catch (IOException e) {
                LogManager.getLogger().error("Could not create JWT", e);
                return Response.serverError().build();
            }
        }).orElseGet(() ->
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        });
    }
}
