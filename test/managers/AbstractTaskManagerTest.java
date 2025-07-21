package managers;

import exeptions.NotFoundException;
import exeptions.ValidationException;
import model.Task;
import model.Epic;
import model.Subtask;
import utils.Status;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractTaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @AfterEach
    void tearDown() {
        manager.removeAllTasks();
    }

    @Test
    void addAndGetTask() {
        Task task = new Task(0, "Task1", "Desc1", Status.NEW,
                LocalDateTime.of(2025, 6, 25, 10, 0), Duration.ofMinutes(60));
        manager.addTask(task);
        Task fetched = manager.getTaskById(task.getId());
        assertEquals(task, fetched, "Добавленная задача должна быть доступна по ID");
    }

    @Test
    void updateTask() {
        Task task = new Task(0, "Task2", "Desc2", Status.NEW,
                LocalDateTime.of(2025, 6, 25, 11, 0), Duration.ofMinutes(30));
        manager.addTask(task);
        task.setName("Updated");
        manager.updateTask(task);
        assertEquals("Updated", manager.getTaskById(task.getId()).getName(),
                "Имя задачи должно обновляться");
    }

    @Test
    void removeAllTasks() {
        manager.addTask(new Task(0, "T3", "D3", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15)));
        manager.removeAllTasks();
        assertTrue(manager.getAllTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    void removeTaskById() {
        Task task = new Task(0, "Task4", "Desc4", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(20));
        manager.addTask(task);
        manager.removeTaskById(task.getId());
        assertThrowsExactly(NotFoundException.class,
                () -> manager.getTaskById(task.getId()),
                "Удаленная задача должна вызывать исключение при попытке доступа");
    }

    // Epic status calculations
    @Test
    void epicStatus_allNew_thenNew() {
        Epic epic = new Epic(0, "Epic1", "E1", Status.NEW,
                LocalDateTime.now(), Duration.ZERO);
        manager.addEpic(epic);
        Subtask s1 = new Subtask(0, "S1", "D", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        Subtask s2 = new Subtask(0, "S2", "D", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(10), epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus(),
                "Если все подзадачи NEW, статус эпика NEW");
    }

    @Test
    void epicStatus_allDone_thenDone() {
        Epic epic = new Epic(0, "Epic2", "E2", Status.NEW,
                LocalDateTime.now(), Duration.ZERO);
        manager.addEpic(epic);
        Subtask s1 = new Subtask(0, "S1", "D", Status.DONE,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        Subtask s2 = new Subtask(0, "S2", "D", Status.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(10), epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus(),
                "Если все подзадачи DONE, статус эпика DONE");
    }

    @Test
    void epicStatus_newAndDone_thenInProgress() {
        Epic epic = new Epic(0, "Epic3", "E3", Status.NEW,
                LocalDateTime.now(), Duration.ZERO);
        manager.addEpic(epic);
        Subtask s1 = new Subtask(0, "S1", "D", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        Subtask s2 = new Subtask(0, "S2", "D", Status.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(10), epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Смешанный NEW и DONE -> IN_PROGRESS");
    }

    @Test
    void epicStatus_anyInProgress_thenInProgress() {
        Epic epic = new Epic(0, "Epic4", "E4", Status.NEW,
                LocalDateTime.now(), Duration.ZERO);
        manager.addEpic(epic);
        Subtask s1 = new Subtask(0, "S1", "D", Status.IN_PROGRESS,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        manager.addSubtask(s1);
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Если есть IN_PROGRESS, статус эпика IN_PROGRESS");
    }

    @Test
    void overlappingTasks_throwException() {
        Task t1 = new Task(0, "T1", "D1", Status.NEW,
                LocalDateTime.of(2025, 6, 25, 9, 0), Duration.ofMinutes(60));
        Task t2 = new Task(0, "T2", "D2", Status.NEW,
                LocalDateTime.of(2025, 6, 25, 9, 30), Duration.ofMinutes(60));
        manager.addTask(t1);
        assertThrows(ValidationException.class, () -> manager.addTask(t2),
                "Пересекающиеся задачи должны приводить к исключению");
    }

    @Test
    void subtaskHasEpic_association() {
        Epic epic = new Epic(0, "Epic5", "E5", Status.NEW,
                LocalDateTime.now(), Duration.ZERO);
        manager.addEpic(epic);
        Subtask sub = new Subtask(0, "Sub1", "D", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15), epic.getId());
        manager.addSubtask(sub);
        List<Subtask> subs = manager.getSubtasksOfEpic(epic.getId());
        assertEquals(1, subs.size(), "Подзадача должна ассоциироваться с эпиком");
        assertEquals(sub, subs.get(0));
    }
}

