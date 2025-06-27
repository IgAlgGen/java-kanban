package managers;

import model.Task;
import org.junit.jupiter.api.*;
import utils.Status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends AbstractTaskManagerTest<FileBackedTaskManager> {
    private File file;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            file = File.createTempFile("tasks", ".csv");
            file.deleteOnExit();
        } catch (IOException e) {
            fail("Не удалось создать временный файл");
        }
        return new FileBackedTaskManager(file);
    }

    @Test
    void saveToFile() {
        Task task = new Task(0, "TASK", "Description", Status.NEW,
                LocalDateTime.of(2025, 6, 25, 12, 0), Duration.ofMinutes(30));
        manager.addTask(task);
        manager.saveToFile();
        String content = assertDoesNotThrow(() -> Files.readString(file.toPath()),
                "Чтение файла не должно выбрасывать исключение");
        assertTrue(content.contains("TASK"), "Файл должен содержать заголовок и записи задач");
    }

    @Test
    void loadFromFile() {
        // Добавляем и сохраняем
        Task task = new Task(0, "FT2", "FD2", Status.NEW,
                LocalDateTime.of(2025, 6, 25, 13, 0), Duration.ofMinutes(45));
        manager.addTask(task);
        manager.saveToFile();
        FileBackedTaskManager loaded = assertDoesNotThrow(
                () -> FileBackedTaskManager.loadFromFile(file),
                "Загрузка из файла не должна выбрасывать исключение");
        assertEquals(1, loaded.getAllTasks().size(), "Должна загрузиться одна задача");
        assertEquals(task.getName(), loaded.getTaskById(task.getId()).getName(),
                "Загруженная задача должна иметь корректные данные");
    }
}