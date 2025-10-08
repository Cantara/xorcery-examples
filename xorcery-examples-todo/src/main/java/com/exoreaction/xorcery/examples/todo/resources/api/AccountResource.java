package com.exoreaction.xorcery.examples.todo.resources.api;

import dev.xorcery.domainevents.command.Command;
import com.exoreaction.xorcery.examples.todo.resources.RequiresAuthentication;
import com.exoreaction.xorcery.examples.todo.resources.TodoApplication;
import dev.xorcery.neo4j.client.GraphDatabase;
import dev.xorcery.neo4j.client.GraphResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.security.auth.Subject;
import java.util.Map;
import java.util.Optional;

@Path("todo/account")
@RequiresAuthentication
public class AccountResource
        extends ThymeleafResource {
    private final ITemplateEngine templateEngine;
    private final TodoApplication todoApplication;
    private final GraphDatabase graphDatabase;

    @Inject
    public AccountResource(ITemplateEngine templateEngine, TodoApplication todoApplication, GraphDatabase graphDatabase) {
        this.templateEngine = templateEngine;
        this.todoApplication = todoApplication;
        this.graphDatabase = graphDatabase;
    }

    @GET
    public Response get() throws Exception {
        Optional<Subject> subjectOpt = getSubject();
        if (subjectOpt.isPresent())
        {
            Subject subject = subjectOpt.get();
            try (GraphResult result = graphDatabase.execute("""
                MATCH (user:User {id:$id})
                RETURN user.id as id, user.email as email
                LIMIT 1
                """, Map.of("id", subject.getPrincipals().stream().findFirst().get().getName()), 30).toCompletableFuture().join()) {

                WebContext context = new WebContext(getWebExchange());
                context.setVariable("user", result.getResult().next());
                return Response.ok(templateEngine.process("account", context)).build();
            }
        } else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}