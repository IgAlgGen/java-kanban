package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Task;
import utils.DurationAdapter;
import utils.LocalDateTimeAdapter;
import utils.Status;
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
class TasksHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    private final String baseUrl = "http://localhost:8080/tasks";

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
    void getAllTasksInitiallyEmpty() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(baseUrl))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }

    @Test
    void createAndGetById() throws Exception {
        // 1. POST новую задачу
        Task t = new Task(0,"Test", "Description", Status.NEW, LocalDateTime.of(2025,1,1, 10,0), Duration.ofMinutes(30));
        String json = gson.toJson(t);
        HttpRequest post = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> postResp = client.send(post, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResp.statusCode());

        // 2. GET all — убедиться, что появилась
        HttpResponse<String> allResp = client.send(
                HttpRequest.newBuilder().GET().uri(URI.create(baseUrl)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        List<?> list = gson.fromJson(allResp.body(), List.class);
        assertEquals(1, list.size());

        // 3. GET по id
        HttpResponse<String> getResp = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "?id=0"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, getResp.statusCode());
        Task fetched = gson.fromJson(getResp.body(), Task.class);
        assertEquals("Test", fetched.getName());
    }

    @Test
    void getNonExistingReturns404() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "?id=999"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }

    @Test
    void deleteByIdAndThenNotFound() throws Exception {
        // Создаём задачу
        Task t = new Task(0, "X", "Y", Status.NEW, LocalDateTime.of(2025,1,1, 10,0), Duration.ofMinutes(30));
        String json = gson.toJson(t);
        client.send(
                HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .uri(URI.create(baseUrl))
                        .header("Content-Type", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        // Удаляем
        HttpResponse<String> del = client.send(
                HttpRequest.newBuilder()
                        .DELETE()
                        .uri(URI.create(baseUrl + "?id=1"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        // Проверяем, что теперь не найдётся
        HttpResponse<String> respNot = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "?id=1"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, respNot.statusCode());
    }
}

