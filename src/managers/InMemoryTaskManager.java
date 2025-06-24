package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;
import utils.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static utils.IdGenerator.*;

/**
 * In-memory реализация TaskManager с поддержкой приоритетной очереди по startTime.
 */
public class InMemoryTaskManager implements TaskManager {
    final Map<Integer, Task> tasks;
    final Map<Integer, Epic> epics;
    final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    /**
     * Отдельное множество для приоритизации задач и подзадач.
     * Сортировка по startTime и затем по id.
     */
    final NavigableSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator
                    .comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                    .thenComparing(Task::getId)
    );

    /**
     * Возвращает  копию списка приоритизированных задач, чтобы избежать изменений в оригинальном множестве.
     * Этот метод позволяет получить задачи в порядке приоритета,
     *
     * @return Список приоритизированных задач.
     */
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks); // O(n) — обход TreeSet без дополнительной сортировки
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
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task); // Удаляем задачи из приоритетной очереди
            }
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
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task); // Добавляем задачу в приоритетную очередь
        }
    }

    @Override
    public void updateTask(Task task) {
        Task old = tasks.get(task.getId());
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old);// Убираем старую версию из приоритизации
            }
            tasks.put(task.getId(), task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);// Добавляем новую, если у неё есть startTime
            }
        }
    }

    @Override
    public void removeTaskById(int id) {
        Task removed = tasks.remove(id);
        historyManager.remove(id);
        if (removed != null && removed.getStartTime() != null) {
            prioritizedTasks.remove(removed);
        }
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
        recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            epic.clearSubtaskIds();
            epic.getSubtaskIDs().addAll(oldEpic.getSubtaskIDs());
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
            recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
        }
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(epic.getId()); // Удаляем эпик из истории
            for (Integer subId : epic.getSubtaskIDs()) {
                historyManager.remove(subId); // Удаляем подзадачи из истории
                Subtask old = subtasks.remove(subId);
                if (old != null && old.getStartTime() != null) {
                    prioritizedTasks.remove(old); // Удаляем подзадачи из приоритетной очереди
                }
            }
        }
    }

    //endregion
    //region Методы для model.Subtask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    /**
     * Удаляет все подзадачи из менеджера.
     * Очищает списки ID подзадач у всех эпиков и пересчитывает их временные параметры.
     * После этого полностью очищает хранилище подзадач.
     */
    @Override
    public void removeAllSubtasks() {
        for (Subtask sub : subtasks.values()) {// Очищаем приоритетное множество и историю
            historyManager.remove(sub.getId());
            if (sub.getStartTime() != null) {
                prioritizedTasks.remove(sub);
            }
        }
        for (Epic epic : epics.values()) {// Очищаем связи в эпиках
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
            recalculateEpicTimeDetails(epic);
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

    /**
     * Добавляет новую подзадачу в менеджер.
     * Генерирует уникальный идентификатор для подзадачи, сохраняет её в хранилище,
     * добавляет идентификатор подзадачи в соответствующий эпик, обновляет статус эпика
     * и пересчитывает его временные параметры.
     *
     * @param subtask подзадача для добавления
     */
    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) { // Привязываем к эпику
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic); // Обновляем статус эпика после добавления подзадачи
            recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
        }
        if (subtask.getStartTime() != null) { // И добавляем в приоритетную очередь
            prioritizedTasks.add(subtask);
        }
    }

    /**
     * Обновляет существующую подзадачу в менеджере.
     * Заменяет подзадачу в хранилище, обновляет статус связанного эпика
     * и пересчитывает временные параметры эпика на основе его подзадач.
     *
     * @param subtask подзадача для обновления
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask old = subtasks.get(subtask.getId());
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old);
            }
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                recalculateEpicTimeDetails(epic);
            }
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
        }
    }

    /**
     * Удаляет подзадачу по её идентификатору.
     *
     * @param id идентификатор подзадачи для удаления
     */
    @Override
    public void removeSubtaskById(int id) {
        Subtask removed = subtasks.remove(id);
        historyManager.remove(id); // Удаляем подзадачу из истории
        if (removed != null && removed.getStartTime() != null) {
            prioritizedTasks.remove(removed);
        }
        if (removed != null) {
            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);// Удаляем подзадачу из эпика, если она была привязана к нему
                updateEpicStatus(epic); // Обновляем статус эпика после удаления подзадачи
                recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
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

    void updateEpicStatus(Epic epic) {
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

    void recalculateEpicTimeDetails(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIDs();
        if (subtaskIds.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }
        epic.setDuration(Duration.ZERO);
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                epic.setDuration(epic.getDuration().plus(subtask.getDuration()));
                if (startTime == null || subtask.getStartTime().isBefore(startTime)) {
                    startTime = subtask.getStartTime();
                }
                LocalDateTime subtaskEndTime = subtask.getEndTime();
                if (endTime == null || subtaskEndTime.isAfter(endTime)) {
                    endTime = subtaskEndTime;
                }
            }
        }
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
    }
}
