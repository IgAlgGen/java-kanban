package server;

import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.Epic;
import exeptions.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public EpicsHandler(TaskManager manager) {
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
                        List<Epic> all = manager.getAllEpics();
                        sendText(exchange, gson.toJson(all), 200);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        Epic epic = manager.getEpicById(id);
                        if (epic == null) {
                            sendNotFound(exchange, "Epic with id=" + id + " not found");
                        } else {
                            sendText(exchange, gson.toJson(epic), 200);
                        }
                    }
                }
                case "POST" -> {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(body, Epic.class);
                    try {
                        if (epic.getId() == 0) {
                            manager.addEpic(epic);
                        } else {
                            manager.updateEpic(epic);
                        }
                        sendText(exchange, "", 201);
                    } catch (ValidationException e) {
                        sendHasOverlaps(exchange, e.getMessage());
                    }
                }
                case "DELETE" -> {
                    if (query == null) {
                        manager.removeAllEpics();
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        manager.removeEpicById(id);
                    }
                    sendText(exchange, "", 200);
                }
                default -> sendServerError(exchange, "Unsupported HTTP method");
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Invalid id format");
        } catch (RuntimeException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(exchange, "Internal error: " + e.getMessage());
        }
    }
}
