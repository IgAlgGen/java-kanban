import managers.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.Managers;
import utils.Status;

import static org.junit.jupiter.api.Assertions.*;

class MasterTest {
    static TaskManager manager;

    @BeforeAll
    static void beforeAll() {
        manager = Managers.getDefault();
        manager.addTask(new Task("Task 1", "Description 1", Status.NEW));
        manager.addTask(new Task("Task 2", "Description 2", Status.NEW));
        manager.addEpic(new Epic("Epic 1", "Description 1", Status.NEW));
        manager.addEpic(new Epic("Epic 2", "Description 2", Status.NEW));
        manager.addSubtask(new Subtask("Subtask 1", "Description 1", Status.NEW, 3));
        manager.addSubtask(new Subtask("Subtask 2", "Description 2", Status.NEW, 3));
        manager.addSubtask(new Subtask("Subtask 3", "Description 3", Status.NEW, 3));
        manager.addSubtask(new Subtask("Subtask 4", "Description 4", Status.DONE, 4));
        manager.addSubtask(new Subtask("Subtask 5", "Description 5", Status.DONE, 4));
    }


    // Проверьте, что экземпляры класса model. Task равны друг другу, если равен их id;
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = manager.getTaskById(1);
        Task task2 = manager.getTaskById(1);
        assertEquals(task1, task2, "Задачи с тем же ID должны быть равны");

    }

    //Проверьте, что наследники класса model. Task равны друг другу, если равен их id;
    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = manager.getSubtaskById(6);
        Subtask subtask2 = manager.getSubtaskById(6);
        assertEquals(subtask1, subtask2, "Подзадачи с тем же ID должны быть равны");
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = manager.getEpicById(3);
        Epic epic2 = manager.getEpicById(3);
        assertEquals(epic1, epic2, "Эпики с тем же ID должны быть равны");
    }

    //Проверьте, что объект model. Epic нельзя добавить в самого себя в виде подзадачи;
    @Test
    void epicShouldNotAddItselfAsSubtask() {
        Epic epic = manager.getEpicById(3);
        Subtask subtask = new Subtask(3, "Subtask 1", "Description 1", Status.NEW, 3);
        manager.addSubtask(subtask);
        Subtask retrievedSubtask = manager.getSubtaskById(subtask.getId());
        assertNotEquals(epic, subtask, "Эпик не должен добавляться в самого себя в виде подзадачи");
        assertNotEquals(subtask.getId(), retrievedSubtask.getEpicId(), "ID эпика не должен совпадать с ID подзадачи");

    }

    //Проверьте, что объект model. Subtask нельзя сделать своим же эпиком;
    @Test
    void subtaskShouldNotBeItsOwnEpic() {
        Subtask subtask = manager.getSubtaskById(6);
        Epic epic = new Epic(6, "Epic 1", "Description 1", Status.NEW);
        manager.addEpic(epic);
        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertNotEquals(subtask, epic, "Подзадача не должна быть своим же эпиком");
        assertNotEquals(subtask.getId(), retrievedEpic.getId(), "ID подзадачи не должен совпадать с ID эпика");
    }

    //Убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
    @Test
    void managersShouldReturnInitializedInstances() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "TaskManager должен быть инициализирован");
        assertTrue(taskManager instanceof TaskManager, "TaskManager должен быть экземпляром TaskManager");
    }

    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
    @Test
    void inMemoryTaskManagerShouldAddAndFindTasks() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        Epic epic = new Epic("Epic 1", "Description 1", Status.NEW);
        Subtask subtask = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId());

        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        assertEquals(task, manager.getTaskById(task.getId()), "Задача должна быть найдена по ID");
        assertEquals(epic, manager.getEpicById(epic.getId()), "Эпик должен быть найден по ID");
        assertEquals(subtask, manager.getSubtaskById(subtask.getId()), "Подзадача должна быть найдена по ID");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    void tasksWithGeneratedIdShouldNotConflict() {
        Task task1 = manager.getTaskById(1);
        Task task2 = new Task(1, "Task 1", "Description 1", Status.NEW);

        manager.addTask(task2);

        assertNotEquals(task1, task2, "Задачи с одинаковым ID не должны конфликтовать");
    }

    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void taskShouldNotChangeWhenAddedToManager() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        manager.addTask(task);

        Task retrievedTask = manager.getTaskById(task.getId());
        assertEquals(task, retrievedTask, "Задача должна оставаться неизменной после добавления в менеджер");
    }

    //убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    void taskShouldRetainPreviousVersionInHistory() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        manager.addTask(task);

        Task retrievedTask = manager.getTaskById(task.getId());
        assertEquals(task, retrievedTask, "Задача должна оставаться неизменной после добавления в менеджер");

        Task task1 = new Task(task.getId(), "Updated Task 1", "Updated Description 1", Status.IN_PROGRESS);
        manager.updateTask(task1);

        Task updatedTask = manager.getTaskById(task.getId());
        assertNotEquals(retrievedTask, updatedTask, "Задача должна быть обновлена в менеджере");
    }
}
