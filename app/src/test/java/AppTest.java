import hexlet.code.App;
import hexlet.code.Controller;
import hexlet.code.model.Url;

import io.ebean.DB;
import io.javalin.Javalin;
import io.javalin.http.Context;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public final class AppTest {
    private static Javalin app;
    private final String baseUrl = "http://localhost:";
    private Context ctx = mock(Context.class);
    private final String correctUrlForTest = "https://ru.hexlet.io";
    private final String inCorrectUrlForTest = "something";

    @BeforeAll
    static void beforeAll() {
        app = App.getApp();
        app.start();
    }

    @AfterAll
    static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void setUp() {
        DB.truncate("urls");
    }

    @Test
    void testMainPage() throws Exception {
        HttpResponse<String> response = Unirest
                .get(baseUrl + app.port())
                .asString();
        int status = response.getStatus();
        assertThat(status).isEqualTo(200);
    }

    @Test
    void testAddUrlPositive() throws Exception {
        HttpResponse<String> response = Unirest
                .post(baseUrl + app.port() + "/urls")
                .field("url", correctUrlForTest)
                .asString();
        int status = response.getStatus();
        boolean checkLocation = response
                .getHeaders()
                .get("Location")
                .contains("/urls");
        boolean checkNameUrlAtDB = DB.find(Url.class)
                .where()
                .eq("name", correctUrlForTest)
                .exists();
        assertThat(checkNameUrlAtDB).isEqualTo(true);
        assertThat(status).isEqualTo(302);
        assertThat(checkLocation).isEqualTo(true);
    }

    @Test
    void testAddUrlIfExist() throws Exception {
        when(ctx.formParam("url")).thenReturn(correctUrlForTest);
        Controller.addUrl.handle(ctx);
        verify(ctx).sessionAttribute("flash", "Страница успешно добавлена");

        ctx = mock(Context.class);
        when(ctx.formParam("url")).thenReturn(correctUrlForTest);
        Controller.addUrl.handle(ctx);
        verify(ctx).sessionAttribute("flash", "Страница уже существует");
        boolean checkNameUrlAtDB = DB.find(Url.class)
                .where()
                .eq("name", inCorrectUrlForTest)
                .findCount() < 2;
        assertThat(checkNameUrlAtDB).isEqualTo(true);
    }

    @Test
    void testAddUrlNegative() throws Exception {
        HttpResponse<String> response = Unirest
                .post(baseUrl + app.port() + "/urls")
                .field("url", inCorrectUrlForTest)
                .asString();
        int status = response.getStatus();
        boolean checkLocation = response
                .getHeaders()
                .get("Location")
                .contains("/");
        assertThat(status).isEqualTo(302);
        assertThat(checkLocation).isEqualTo(true);

        DB.truncate("urls");

        when(ctx.formParam("url")).thenReturn(inCorrectUrlForTest);
        Controller.addUrl.handle(ctx);
        verify(ctx).sessionAttribute("flash", "Некорректный URL");
    }

    @Test
    void testShowUrls() throws Exception {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + app.port() + "/urls")
                .field("url", correctUrlForTest)
                .asString();
        HttpResponse<String> responseGet = Unirest
                .get(baseUrl + app.port() + "/urls")
                .asString();
        int status = responseGet.getStatus();
        assertThat(status).isEqualTo(200);

        List<Url> urls = DB.find(Url.class)
                .findList();
        Controller.showUrls.handle(ctx);
        verify(ctx).attribute("urls", urls);
    }

    @Test
    void testShowUrl() throws Exception {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + app.port() + "/urls")
                .field("url", correctUrlForTest)
                .asString();
        Url url = DB.find(Url.class)
                .where()
                .eq("name", correctUrlForTest)
                .findOne();
        HttpResponse<String> responseGet = Unirest
                .get(baseUrl + app.port() + "/urls/" + url.getId())
                .asString();
        int status = responseGet.getStatus();
        assertThat(status).isEqualTo(200);

        when(ctx.path()).thenReturn("/urls/" + url.getId());
        Controller.showUrl.handle(ctx);
        verify(ctx).attribute("url", url);
    }
}
