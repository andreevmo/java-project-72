package hexlet.code;

import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;

public final class Controller {

    public static Handler mainPage = ctx -> {
        ctx.render("mainPage.html");
    };

    public static Handler addUrl = ctx -> {
        try {
            URL url = new URL(ctx.formParam("url"));
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("mainPage.html");
        }
    };
}
