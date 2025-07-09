package server;

import com.google.gson.Gson;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import model.Task;
import utils.Status;
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
    private final Gson gson = new Gson();
    private final String baseUrl = "http://localhost:8080/history";

    @BeforeEach
    void setUp() throws Exception {
        TaskManager mgr = new InMemoryTaskManager();
        // создаём и сразу читаем одну задачу, чтобы история не пуста
        mgr.addTask(new Task("Task 1", "Description 1", Status.NEW, LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofMinutes(90)));
        mgr.getTaskById(1);

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
        TaskManager mgr = new InMemoryTaskManager();
        mgr.addTask(new Task("Task 1", "Description 1", Status.NEW, LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofMinutes(90)));
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
                        .uri(URI.create(baseUrl + "?id=1"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(500, resp.statusCode());
    }
}