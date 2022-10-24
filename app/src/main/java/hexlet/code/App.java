package hexlet.code;

import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;

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
        });
        addRoutes(app);
        return app;
    }

    public static void addRoutes(Javalin app) {
        app.routes(() -> {
            path("/", () -> {
                get(ctx -> {ctx.result("Hello world!");});
            });
        });
    }

    public static void main(String[] args) {
        getApp().start(getPort());
    }
}
