package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import managers.TaskManager;
import utils.Managers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer() throws Exception {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        // берём реализацию менеджера
        taskManager = Managers.getDefault();

        // регистрируем обработчики, передавая экземпляр менеджера
        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));

        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.taskManager = manager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));

        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer;
        try {
            httpTaskServer = new HttpTaskServer();
            httpTaskServer.start();
            System.out.println("HTTP-сервер запущен");
        } catch (Exception e) {
            System.err.println("Ошибка при запуске HTTP-сервера: " + e.getMessage());
        }
    }

    static class StubHandler implements HttpHandler {
        private final String message;

        public StubHandler(String message) {
            this.message = message;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = message;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
