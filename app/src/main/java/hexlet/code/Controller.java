package hexlet.code;

import io.javalin.http.Handler;

public final class Controller {

    public static Handler mainPage = ctx -> {
        ctx.render("mainPage.html");
    };
}
