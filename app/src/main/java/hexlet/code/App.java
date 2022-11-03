package hexlet.code;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import static io.javalin.apibuilder.ApiBuilder.*;

public class App {

    public static int getPort() {
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
        return app;
    }

    public static void addRoutes(Javalin app) {
        app.routes(() -> {
            path("/", () -> get(Controller.mainPage));
            path("/urls", () -> post(Controller.addUrl));
        });
    }

    public static void main(String[] args) {
        getApp().start(getPort());
    }
}
