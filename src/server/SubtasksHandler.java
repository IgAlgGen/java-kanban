package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exeptions.NotFoundException;
import managers.TaskManager;
import model.Subtask;
import exeptions.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public SubtasksHandler(TaskManager manager) {
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
                        List<Subtask> all = manager.getAllSubtasks();
                        sendText(exchange, gson.toJson(all), 200);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        Subtask sub = manager.getSubtaskById(id);
                        sendText(exchange, gson.toJson(sub), 200);
                    }
                }
                case "POST" -> {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    try {
                        if (subtask.getId() == 0) {
                            manager.addSubtask(subtask);
                        } else {
                            manager.updateSubtask(subtask);
                        }
                        sendText(exchange, "", 201);
                    } catch (ValidationException e) {
                        sendHasOverlaps(exchange, e.getMessage());
                    }
                }
                case "DELETE" -> {
                    if (query == null) {
                        manager.removeAllSubtasks();
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        manager.removeSubtaskById(id);
                    }
                    sendText(exchange, "", 200);
                }
                default -> sendServerError(exchange, "Неподдерживаемый метод HTTP");
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат идентификатора");
        } catch (Exception e) {
            sendServerError(exchange, "Внутренняя ошибка:" + e.getMessage());
        }
    }
}
