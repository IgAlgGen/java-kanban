package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public HistoryHandler(TaskManager manager) {
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
            sendServerError(exchange, "History endpoint does not accept query parameters");
            return;
        }
        List<Task> history = manager.getFromHistory();
        sendText(exchange, gson.toJson(history), 200);
    }
}
