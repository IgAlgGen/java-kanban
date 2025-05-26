package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;
import utils.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.IdGenerator.*;

/**
 * Я сначала делал обобщённый менеджер по типу задач:
 *      public class InMemoryTaskManager<T extends Task> implements TaskManager<T>{
 *      private final Map<Integer, T> storage = new HashMap<>();
 *      и т.д.
 *      }
 * Удобство в повторно используемом коде для любых потомков Task и универсальности кода.
 * Большое неудобство в том, что трудно управлять взаимосвязью, например между Epic и Subtask,
 * потому что InMemoryTaskManager<Subtask> не знает, к какому Epic относится Subtask.

 * Ну или я не придумал как реализовать.

 * Оставил реализацию с не параметризованным интерфейсом, но отдельными методами.
 *      Тут проще управлять связями между типами задач
 *      Да, дублируется часть логики.
 *      И да, не используются дженерики.
 */
public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    private void addToHistory(Task task) {
        historyManager.add(task);
    }

    @Override
    public List<Task> getFromHistory() {
        return historyManager.getHistory();
    }

    //region Методы для model.Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId()); // Удаляем задачи из истории
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            addToHistory(task);
        }
        return task;
    }

    @Override
    public void addTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void removeTaskById(int id) {
        historyManager.remove(id); // Удаляем задачу из истории
        tasks.remove(id);
    }

    //endregion
    //region Методы для model.Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer subtaskId : epic.getSubtaskIDs()) {
                historyManager.remove(subtaskId); // Удаляем подзадачи из истории
            }
            historyManager.remove(epic.getId()); // Удаляем эпики из истории
        }
        epics.clear(); // Эпики удаляются
        subtasks.clear(); // Подзадачи эпиков тоже удаляются


    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            addToHistory(epic);
        }
        return epic;
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            epic.clearSubtaskIds();
            epic.getSubtaskIDs().addAll(oldEpic.getSubtaskIDs());
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
        }
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(epic.getId()); // Удаляем эпик из истории
            for (Integer subId : epic.getSubtaskIDs()) {
                historyManager.remove(subId); // Удаляем подзадачи из истории
                subtasks.remove(subId);
            }
        }
    }

    //endregion
    //region Методы для model.Subtask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
        }
        subtasks.clear();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            addToHistory(subtask);
        }
        return subtask;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        historyManager.remove(id); // Удаляем подзадачу из истории
         // Удаляем подзадачу из эпика, если она была привязана к нему
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIDs()) {
                result.add(subtasks.get(subtaskId));
            }
        }
        return result;
    }

    //endregion

    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIDs();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (Integer id : subtaskIds) {
            Status status = subtasks.get(id).getStatus();
            if (status != Status.NEW) {
                allNew = false;
            }
            if (status != Status.DONE) {
                allDone = false;
            }
        }
        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
