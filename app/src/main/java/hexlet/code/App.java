package hexlet.code;

import hexlet.code.controller.Controller;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;


public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.valueOf(port);
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        return templateEngine;
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
            JavalinThymeleaf.init(getTemplateEngine());
        });
        addRoutes(app);
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });
        return app;
    }

    private static void addRoutes(Javalin app) {
        app.routes(() -> {
            path("/", () -> get(Controller.mainPage));
            path("/urls", () -> {
                post(Controller.addUrl);
                get(Controller.showUrls);
                path("{id}", () -> {
                    get(Controller.showUrl);
                    path("/checks", () -> {
                        post(Controller.addUrlChecks);
                    });
                });
            });
        });
    }

    public static void main(String[] args) {
        getApp().start(getPort());
    }
}
