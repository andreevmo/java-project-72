package hexlet.code;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;


public class App {

    private static int getPort() {
        String port = System.getenv("PORT");
        if (port != null) {
            return Integer.valueOf(port);
        }
        return 5000;
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
            JavalinThymeleaf.init(GeneratorTemplateEngine.getInstance());
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
                });
            });
        });
    }

    public static void main(String[] args) {
        getApp().start(getPort());
    }
}
