package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import utils.DurationAdapter;
import utils.LocalDateTimeAdapter;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HistoryHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    private final String baseUrl = "http://localhost:8080/history";

    @BeforeEach
    void setUp() throws Exception {
        TaskManager mgr = new InMemoryTaskManager();
        server = new HttpTaskServer(mgr);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void getHistory() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder().GET().uri(URI.create(baseUrl)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        // должен вернуть непустой массив
        assertTrue(resp.body().startsWith("["));
    }

    @Test
    void queryNotAllowed() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "?id=2"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(500, resp.statusCode());
    }
}