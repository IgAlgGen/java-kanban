package managers;

import model.Task;
import org.junit.jupiter.api.*;
import utils.Status;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasksTest", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void saveToFile_writesTasksToFile() throws IOException {
        Task task = new Task(1, "Test Task", "Description", Status.NEW);
        manager.addTask(task);

        // Проверяем, что файл содержит строку задачи
        String content = Files.readString(tempFile.toPath());
        assertTrue(content.contains("Test Task"));
        assertTrue(content.contains("Description"));
        assertTrue(content.contains("NEW"));
    }


    @Test
    void loadFromFile_loadsTasksFromFile() throws IOException {
        // Сохраняем задачу в файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("id,type,name,status,description,epic\n");
            writer.write("1,TASK,Test Task,NEW,Description,\n");
            writer.newLine();
        }

        // Загружаем задачи из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals("Test Task", loadedManager.getAllTasks().get(0).getName());
    }
}
