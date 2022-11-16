package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class Controller {

    public static Handler mainPage = ctx -> {
        ctx.render("mainPage.html");
    };

    public static Handler addUrl = ctx -> {
        String inputUrl = ctx.formParam("url");
        URL parseUrl;
        try {
            parseUrl = new URL(inputUrl);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String nameUrl = getNameForUrl(parseUrl);
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
                .orderBy("created_at")
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
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl()
                .where()
                .id.eq(id)
                .findOne();
        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);

        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();
            Integer statusCode = response.getStatus();
            Document document = Jsoup.parse(response.getBody());
            String title = document.title();
            String h1 = document.getElementsByTag("h1").text();
            String description = document.select("meta[name=description]").attr("content");

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
            urlCheck.save();
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.attribute("urlCheck", urlCheck);
            ctx.sessionAttribute("flash-type", "success");
            //ctx.render("showUrl.html");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", " Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/urls/" + url.getId());
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + url.getId());
    };

    private static String getNameForUrl(URL url) {
        return url.getPort() == -1
                ? url.getProtocol() + "://" + url.getHost()
                : url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
    }
}
