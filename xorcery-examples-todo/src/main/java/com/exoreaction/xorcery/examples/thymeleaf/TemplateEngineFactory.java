package com.exoreaction.xorcery.examples.thymeleaf;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.hk2.api.Factory;
import org.jvnet.hk2.annotations.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;


@Service(name = "thymeleaf")
public class TemplateEngineFactory
        implements Factory<ITemplateEngine> {

    private final TemplateEngine templateEngine;

    @Inject
    public TemplateEngineFactory(ServletContextHandler servletContextHandler) {

        final WebApplicationTemplateResolver templateResolver =
                new WebApplicationTemplateResolver(JakartaServletWebApplication.buildApplication(servletContextHandler.getServletContext()));

        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // This will convert "home" to "/WEB-INF/templates/home.html"
        templateResolver.setPrefix("WEB-INF/templates/");
        templateResolver.setSuffix(".html");

        // Cache is set to true by default. Set to false if you want templates to
        // be automatically updated when modified.
        templateResolver.setCacheable(false);
        // Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
        templateResolver.setCacheTTLMs(3600000L);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    @Singleton
    public ITemplateEngine provide()  {
        return templateEngine;
    }

    @Override
    public void dispose(ITemplateEngine instance) {
    }
}
