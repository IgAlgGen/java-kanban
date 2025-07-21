package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import exeptions.NotFoundException;
import managers.TaskManager;
import model.Task;
import utils.DurationAdapter;
import utils.LocalDateTimeAdapter;
import exeptions.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();

            switch (method) {
                case "GET" -> {
                    if (query == null) {
                        List<Task> all = manager.getAllTasks();
                        String json = gson.toJson(all);
                        sendText(exchange, json, 200);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        Task task = manager.getTaskById(id);
                        sendText(exchange, gson.toJson(task), 200);

                    }
                }

                case "POST" -> {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Task task = gson.fromJson(body, Task.class);
                    try {
                        if (task.getId() == 0) {
                            manager.addTask(task);
                        } else {
                            manager.updateTask(task);
                        }
                        sendText(exchange, "", 201);
                    } catch (ValidationException e) {
                        sendHasOverlaps(exchange, e.getMessage());
                    }
                }

                case "DELETE" -> {
                    if (query == null) {
                        manager.removeAllTasks();
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        manager.removeTaskById(id);
                    }
                    sendText(exchange, "", 200);
                }
                default -> sendServerError(exchange, "Неподдерживаемый метод HTTP");
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат идентификатора задачи");
        } catch (Exception e) {
            sendServerError(exchange, "Внутренняя ошибка:" + e.getMessage());
        }
    }
}
