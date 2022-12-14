package hexlet.code;

import hexlet.code.controller.Controller;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.validation.Validator;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public final class AppTest {
    private static Javalin app;
    private final String baseUrl = "http://localhost:";
    private Context ctx;
    private final String correctUrlForTest = "https://ru.hexlet.io";
    private final String inCorrectUrlForTest = "something";
    private static MockWebServer server = new MockWebServer();
    String bodyForTest;

    private Transaction transaction;
    @BeforeAll
    static void beforeAll() throws IOException {
        app = App.getApp();
        app.start();
        server.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        app.stop();
        server.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        Url url = DB.find(Url.class)
                .where()
                .eq("name", correctUrlForTest)
                .findOne();
        if (url != null) {
            DB.delete(url);
        }
        ctx = mock(Context.class);
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
        verify(ctx).sessionAttribute("flash", "???????????????? ?????????????? ??????????????????");

        ctx = mock(Context.class);
        when(ctx.formParam("url")).thenReturn(correctUrlForTest);
        Controller.addUrl.handle(ctx);
        verify(ctx).sessionAttribute("flash", "???????????????? ?????? ????????????????????");
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

        when(ctx.formParam("url")).thenReturn(inCorrectUrlForTest);
        Controller.addUrl.handle(ctx);
        verify(ctx).sessionAttribute("flash", "???????????????????????? URL");
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

        urls = DB.find(Url.class)
                .where()
                .between("id", 13, 24)
                .findList();
        when(ctx.queryParam("page")).thenReturn("2");
        Controller.showUrls.handle(ctx);
        verify(ctx).attribute("urls", urls);
    }

    @Test
    void testShowUrlPositive() throws Exception {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + app.port() + "/urls")
                .field("url", correctUrlForTest + ":" + "8000")
                .asString();
        Url url = DB.find(Url.class)
                .where()
                .eq("name", correctUrlForTest + ":" + "8000")
                .findOne();
        HttpResponse<String> responseGet = Unirest
                .get(baseUrl + app.port() + "/urls/" + url.getId())
                .asString();
        int status = responseGet.getStatus();
        assertThat(status).isEqualTo(200);

        when(ctx.pathParamAsClass("id", Integer.class))
                .thenReturn(Validator.create(Integer.class, String.valueOf(url.getId()), "id"));
        Controller.showUrl.handle(ctx);
        verify(ctx).attribute("url", url);
    }

    @Test
    void testShowUrlNegative() {
        HttpResponse<String> responseGet = Unirest
                .get(baseUrl + app.port() + "/urls/1")
                .asString();
        int status = responseGet.getStatus();
        assertThat(status).isEqualTo(404);
    }

    @Test
    void testAddUrlCheckPositive() throws Exception {
        bodyForTest = Files.readString(Path.of("src/test/resources/bodyForTest.html"));
        server.enqueue(new MockResponse().setBody(bodyForTest));
        String url = server.url("/UrlCheck").toString();
        System.out.println(url);
        Url urlForTest = new Url(url);
        urlForTest.save();
        when(ctx.pathParamAsClass("id", Integer.class))
                .thenReturn(Validator.create(Integer.class, String.valueOf(urlForTest.getId()), "id"));
        Controller.addUrlChecks.handle(ctx);

        UrlCheck urlCheck = DB.find(UrlCheck.class)
                .where()
                .eq("url_id", urlForTest.getId())
                .findOne();
        assertThat(urlCheck).isNotNull();
        assertThat(urlCheck.getCreatedAt()).isNotNull();
        assertThat(urlCheck.getStatusCode()).isEqualTo(200);
        assertThat(urlCheck.getH1())
                .isEqualTo("????????????-?????????? ????????????????????????????????, ???? ???????????????????????? ?????????????? ???????????????? ????????????????");
        assertThat(urlCheck.getTitle()).isEqualTo("?????????????? ??? ???????????? ?????? ?????????? ????????????????????????????????. "
                        + "???????????? ??????????, ???????????????????? ??????????????????????????");
        assertThat(urlCheck.getDescription()).isEqualTo("?????????? ???????????? ???????????????????? ?????????????????????????? "
                + "?? ?????????????????????????? ???? JS, Python, Java, PHP, Ruby. "
                + "?????????????????? ?????????????????? ???????????????? ?? ?????????????????? ?? ???????????????? ?????????????????? ?? ????????????. "
                + "???????????? ?? ?????????????????????????????? ?????????? ?????????????????? ?????????????????? ????????????????");
    }

    @Test
    void testAddUrlCheckNegative() throws Exception {
        HttpResponse<String> response = Unirest
                .post(baseUrl + app.port() + "/urls/123/checks")
                .asString();
        assertThat(response.getStatus()).isEqualTo(404);
    }
}
