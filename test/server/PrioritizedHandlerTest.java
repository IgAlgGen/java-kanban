package server;

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
class PrioritizedHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/prioritized";

    @BeforeEach
    void setUp() throws Exception {
        TaskManager mgr = new InMemoryTaskManager();
        // добавим пару задач с разными приоритетами
        mgr.addTask(new Task("Task 1", "Description 1", Status.NEW, LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofMinutes(30)));
        mgr.addTask(new Task("Task 2", "Description 2", Status.NEW, LocalDateTime.of(2025, 1, 2, 10, 0), Duration.ofMinutes(30)));
        server = new HttpTaskServer(mgr);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void getPrioritized() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder().GET().uri(URI.create(baseUrl)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().startsWith("["));
    }

    @Test
    void queryNotAllowed() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "?foo=bar"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(500, resp.statusCode());
    }
}