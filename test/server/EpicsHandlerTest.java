package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.*;
import utils.DurationAdapter;
import utils.LocalDateTimeAdapter;
import utils.Status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EpicsHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    private final String baseUrl = "http://localhost:8080/epics";

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
    void getEmptyEpics() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder().GET().uri(URI.create(baseUrl)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }

    @Test
    void createAndGetEpic() throws Exception {
        Epic e = new Epic("Epic 1", "Description 1", Status.NEW, Duration.ofMinutes(120), LocalDateTime.of(2025, 1, 1, 14, 0));
        String json = gson.toJson(e);
        HttpResponse<String> post = client.send(
                HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .uri(URI.create(baseUrl))
                        .header("Content-Type", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, post.statusCode());

        HttpResponse<String> all = client.send(
                HttpRequest.newBuilder().GET().uri(URI.create(baseUrl)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        List<?> list = gson.fromJson(all.body(), List.class);
        assertEquals(1, list.size());
    }
}