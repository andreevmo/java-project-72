import hexlet.code.App;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    private static Javalin app;
    private static String baseUrl;

    @BeforeAll static void beforeAll() {
        app = App.getApp();
        app.start();
        baseUrl = "http://localhost:" + app.port();
    }

    @AfterAll static void afterAll() {
        app.stop();
    }

    @Test void testApp() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/")
                .asString();
        String content  = response.getBody();
        int answerCode = response.getStatus();

        assertThat(answerCode).isEqualTo(200);
    }
}
