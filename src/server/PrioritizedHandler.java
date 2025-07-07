package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendServerError(exchange, "Only GET supported");
            return;
        }
        URI uri = exchange.getRequestURI();
        if (uri.getQuery() != null) {
            sendServerError(exchange, "Prioritized endpoint does not accept query parameters");
            return;
        }
        List<Task> prioritized = manager.getPrioritizedTasks();
        sendText(exchange, gson.toJson(prioritized), 200);
    }
}
