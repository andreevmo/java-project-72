package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import io.ebean.DB;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class Controller {

    public static Handler mainPage = ctx -> {
        ctx.render("mainPage.html");
    };

    public static Handler addUrl = ctx -> {
        try {
            String nameUrl = getNameForUrl(ctx.formParam("url"));
            boolean checkUrl = new QUrl()
                    .name.eq(nameUrl)
                    .exists();

            if (checkUrl) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/");
                return;
            }

            Url modelUrl = new Url(nameUrl);
            modelUrl.save();

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
            ctx.redirect("/urls");
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
        }
    };
    public static Handler showUrls = ctx -> {
        int step = 12;
        int limit = step - 1;
        int offset = 0;
        String pageFromUrl = ctx.queryParam("page");

        if (pageFromUrl != null) {
            offset = step * Integer.parseInt(pageFromUrl) - step;
            limit = step * Integer.parseInt(pageFromUrl) - 1;

        }

        List<Url> urls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(limit)
                .findList();

        ctx.attribute("urls", urls);
        ctx.render("showUrls.html");
    };
    public static Handler showUrl = ctx -> {
        String id = ctx.pathParam("id");
        Url url = new QUrl()
                .id.eq(Integer.valueOf(id))
                .findOne();
        ctx.attribute("url", url);
        ctx.render("showUrl.html");
    };
    public static Handler addUrlChecks = ctx -> {
        String id = ctx.pathParam("id");
        Url url = new QUrl()
                .where()
                .id.eq(Integer.parseInt(id))
                .findOne();
        HttpResponse<String> response = Unirest
                .get(url.getName())
                .asString();

        int statusCode = response.getStatus();
        UrlCheck urlCheck = new UrlCheck(statusCode, "", "", "", url);
        urlCheck.save();
        ctx.attribute("url", url);
        ctx.attribute("urlCheck", urlCheck);
        ctx.render("showUrl.html");
    };

    private static String getNameForUrl(String url) throws MalformedURLException {
        URL urlForParse = new URL(url);
        return urlForParse.getPort() == -1
                ? urlForParse.getProtocol() + "://" + urlForParse.getHost()
                : urlForParse.getProtocol() + "://" + urlForParse.getHost() + ":" + urlForParse.getPort();
    }
}
