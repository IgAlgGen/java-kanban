import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import managers.FileBackedTaskManager;
import managers.TaskManager;
import utils.Managers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer() throws Exception {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        // Привязка обработчиков к базовым путям
        server.createContext("/tasks", new StubHandler("Tasks endpoint"));
        server.createContext("/subtasks", new StubHandler("Subtasks endpoint"));
        server.createContext("/epics", new StubHandler("Epics endpoint"));
        server.createContext("/history", new StubHandler("History endpoint"));
        server.createContext("/prioritized", new StubHandler("Prioritized endpoint"));

        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }
    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(2);
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
