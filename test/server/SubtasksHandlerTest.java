package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Subtask;
import utils.DurationAdapter;
import utils.LocalDateTimeAdapter;
import utils.Status;
import utils.TaskType;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubtasksHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    private final String baseUrl = "http://localhost:8080/subtasks";

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
    void getEmptyList() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder().GET().uri(URI.create(baseUrl)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }

    @Test
    void createAndFetch() throws Exception {
        Subtask s = new Subtask("Subtask 1", "Description 1", Status.NEW, LocalDateTime.of(2025, 1, 1, 18, 0), Duration.ofMinutes(30), 0);
        String json = gson.toJson(s);
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

    @Test
    void fetchInvalidId() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "?id=42"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }
}
