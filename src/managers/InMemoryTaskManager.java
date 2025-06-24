package managers;

import exeptions.ValidationException;
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
     * Возвращает копию списка приоритизированных задач, чтобы избежать изменений в оригинальном множестве.
     * Этот метод позволяет получить задачи в порядке приоритета,
     *
     * @return Список приоритизированных задач.
     */
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks); // O(n) — обход TreeSet без дополнительной сортировки
    }

    /**
     * Проверяет, пересекаются ли интервалы двух задач.
     *
     * @param a задача a
     * @param b задача b
     * @return Возвращает true, если интервалы [a.start, a.end) и [b.start, b.end) пересекаются.
     */
    private boolean isIntersect(Task a, Task b) {
        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();
        if (aStart == null || bStart == null) {// если у одной нет времени — пересечения нет
            return false;
        }
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);// пересекаются, если начало одного раньше конца другого и наоборот
    }

    /**
     * Проверяет, есть ли пересечение между задачей и другими задачами в приоритетной очереди.
     *
     * @param t задача, которую нужно проверить на пересечение с другими задачами
     * @return Возвращает true, если есть пересечение с другими задачами в приоритетной очереди.
     */
    private boolean hasIntersection(Task t) {
        return getPrioritizedTasks().stream()
                .filter(other -> other.getId() != t.getId())// не сравниваем задачу саму с собой
                .anyMatch(other -> isIntersect(other, t));
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

    /**
     * Добавляет новую задачу в менеджер.<br>
     * Генерирует уникальный идентификатор для задачи, проверяет на пересечение с другими задачами,
     * и если пересечений нет, добавляет задачу в приоритетную очередь и в хранилище задач.<br>
     * Если задача уже существует, то обновляет её идентификатор и добавляет в приоритетную очередь, если у неё есть время начала.<br>
     * Если задача пересекается по времени с существующей задачей, выбрасывает исключение ValidationException.
     *
     * @param task Задача, которую нужно добавить в менеджер.
     */
    @Override
    public void addTask(Task task) {
        task.setId(generateId());
        if (task.getStartTime() != null) {
            if (hasIntersection(task)) {
                throw new ValidationException("Задача пересекается по времени с существующей");
            }
            prioritizedTasks.add(task);
        }
        tasks.put(task.getId(), task);
    }

    /**
     * Обновляет существующую задачу в менеджере.<br>
     * Удаляет старую версию задачи из приоритетной очереди, если у неё было время начала,
     * проверяет на пересечение с другими задачами,
     * и если пересечений нет, добавляет новую версию задачи в приоритетную очередь и обновляет хранилище задач.<br>
     * Если задача пересекается по времени с существующей задачей, выбрасывает исключение ValidationException.
     *
     * @param task Задача, которую нужно обновить в менеджере.<br>
     */
    @Override
    public void updateTask(Task task) {
        Task old = tasks.get(task.getId());
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old);// удаляем из prioritizedTasks старую версию, если была
            }
            if (task.getStartTime() != null && hasIntersection(task)) {// перед вставкой новой — проверяем пересечение
                prioritizedTasks.add(old);// не забываем вернуть старую в множество, если хотим откатить
                throw new ValidationException("Задача пересекается по времени с существующей");
            }
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);// всё ок — вставляем
            }
            tasks.put(task.getId(), task); // обновляем задачу в хранилище
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

    /**
     * Обновляет статус эпика на основе статусов его подзадач.
     * Если все подзадачи новые, эпик становится новым.
     * Если все подзадачи выполнены, эпик становится выполненным.
     * Если есть хотя бы одна новая и одна выполненная подзадача, эпик становится в процессе выполнения.
     *
     * @param epic Эпик, статус которого нужно обновить.
     */
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

    /**
     * Пересчитывает временные параметры эпика на основе его подзадач.
     * Если у эпика нет подзадач, устанавливает продолжительность в 0 и время начала/окончания в null.
     * Иначе суммирует продолжительности подзадач и определяет минимальное время начала и максимальное время окончания.
     *
     * @param epic Эпик, для которого нужно пересчитать временные параметры.
     */
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
     * Добавляет новую подзадачу в менеджер.<br>
     * Генерирует уникальный идентификатор для подзадачи, проверяет на пересечение с другими подзадачами,
     * и если пересечений нет, добавляет подзадачу в приоритетную очередь и в хранилище подзадач.<br>
     * Если подзадача уже существует, то обновляет её идентификатор и добавляет в приоритетную очередь, если у неё есть время начала.<br>
     * Если подзадача пересекается по времени с существующей, выбрасывает исключение ValidationException.<br>
     * * Также привязывает подзадачу к соответствующему эпику, обновляет статус эпика и пересчитывает его временные параметры.
     *
     * @param subtask подзадача для добавления
     */
    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        if (subtask.getStartTime() != null) {
            if (hasIntersection(subtask)) {
                throw new ValidationException("Подзадача пересекается по времени с существующей");
            }
            prioritizedTasks.add(subtask);// Добавляем в приоритетную очередь
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) { // Привязываем к эпику
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic); // Обновляем статус эпика после добавления подзадачи
            recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
        }
    }

    /**
     * Обновляет существующую подзадачу в менеджере.<br>
     * Удаляет старую версию подзадачи из приоритетной очереди, если у неё было время начала,
     * проверяет на пересечение с другими подзадачами,
     * и если пересечений нет, добавляет новую версию подзадачи в приоритетную очередь и обновляет хранилище подзадач.<br>
     * Если подзадача пересекается по времени с существующей, выбрасывает исключение ValidationException.<br>
     * * Также обновляет статус эпика, к которому привязана подзадача, и пересчитывает его временные параметры.<br>
     * * Этот метод позволяет обновить подзадачу, сохраняя её связь с эпиком и корректно обновляя статусы и временные параметры эпика.
     *
     * @param subtask подзадача для обновления
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask old = subtasks.get(subtask.getId());
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old);// Удаляем старую версию из приоритетной очереди, если была
            }
            if (subtask.getStartTime() != null && hasIntersection(subtask)) {
                prioritizedTasks.add(old); // Возвращаем старую версию, если пересекается
                throw new ValidationException("Подзадача пересекается по времени с существующей");
            }
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);// Добавляем новую версию в приоритетную очередь
            }
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());// Получаем эпик, к которому привязана подзадача
            if (epic != null) {
                updateEpicStatus(epic);// Обновляем статус эпика после обновления подзадачи
                recalculateEpicTimeDetails(epic);// Пересчитываем временные параметры эпика
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


}
