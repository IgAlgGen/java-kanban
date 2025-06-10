package utils;

import managers.*;

import java.nio.file.Path;

public final class Managers {
    private Managers() {
        // Частный конструктор для предотвращения экземпляров
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTaskManager getFileBackedTaskManager(Path filePath) {
        return new FileBackedTaskManager(filePath.toFile());
    }
}
