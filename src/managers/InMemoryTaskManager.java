package managers;

import exeptions.ValidationException;
import exeptions.NotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;
import utils.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Optional;

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
        if (aStart == null || bStart == null) { // если у одной нет времени — пересечения нет
            return false;
        }
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd); // пересекаются, если начало одного раньше конца другого и наоборот
    }

    /**
     * Проверяет, есть ли пересечение между задачей и другими задачами в приоритетной очереди.
     *
     * @param t задача, которую нужно проверить на пересечение с другими задачами
     * @return Возвращает true, если есть пересечение с другими задачами в приоритетной очереди.
     */
    private boolean hasIntersection(Task t) {
        return getPrioritizedTasks().stream()
                .filter(other -> other.getId() != t.getId()) // не сравниваем задачу саму с собой
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
        tasks.values().forEach(t -> {
            historyManager.remove(t.getId()); // удаляем задачу из истории
            if (t.getStartTime() != null) prioritizedTasks.remove(t); // удаляем из приоритетной очереди, если была
        });
        tasks.clear(); // очищаем хранилище задач
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            addToHistory(task);
        } else {
            throw new NotFoundException("Задача с id = " + id + " не найдена");
        }
        return task;
    }

    /**
     * Добавляет новую задачу в менеджер.<br>
     * Генерирует уникальный идентификатор для задачи, проверяет на пересечение с другими задачами,
     * и если пересечений нет, добавляет задачу в приоритетную очередь и в хранилище задач.<br>
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
        if (!tasks.containsKey(task.getId())) {
            throw new NotFoundException("Невозможно обновить: задачу с id =" + task.getId() + " не найдена");
        }
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old); // удаляем из prioritizedTasks старую версию, если была
            }
            if (task.getStartTime() != null && hasIntersection(task)) { // перед вставкой новой — проверяем пересечение
                prioritizedTasks.add(old); // не забываем вернуть старую в множество, если хотим откатить
                throw new ValidationException("Задача пересекается по времени с существующей");
            }
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task); // всё ок — вставляем
            }
            tasks.put(task.getId(), task); // обновляем задачу в хранилище
        }
    }

    @Override
    public void removeTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed == null) {
            throw new NotFoundException("Невозможно удалить: задача с id = " + id + " не найдена");
        } else if (removed.getStartTime() != null) {
            prioritizedTasks.remove(removed);
        } else {
            historyManager.remove(id);
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
        epics.values().forEach(e -> historyManager.remove(e.getId())); // удаляем эпики из истории
        epics.clear(); // очищаем хранилище эпиков
        removeAllSubtasks(); // удаляем все подзадачи, связанные с эпиками
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с id = " + id + " не найден");
        } else {
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

    /**
     * Обновляет существующий эпик в менеджере.<br>
     * Очищает ID подзадач, чтобы избежать дублирования, сохраняет старые ID подзадач,
     * и обновляет эпик в хранилище.<br>
     * После обновления эпика пересчитывает его статус и временные параметры.<br>
     * Этот метод позволяет обновить эпик, сохраняя связь с подзадачами и корректно обновляя статусы и временные параметры эпика.
     *
     * @param epic Эпик, который нужно обновить в менеджере.<br>
     */
    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NotFoundException("Невозможно обновить: эпик с id = " + epic.getId() + " не найден");
        }
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            epic.clearSubtaskIds(); // Очищаем ID подзадач, чтобы избежать дублирования
            epic.getSubtaskIDs().addAll(oldEpic.getSubtaskIDs()); // добавляем прежние ID подзадач
            epics.put(epic.getId(), epic); // Обновляем эпик в хранилище
            updateEpicStatus(epic); // Обновляем статус эпика после обновления
            recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
        }
    }

    /**
     * Удаляет эпик по его идентификатору.<br>
     * Удаляет эпик из хранилища, очищает историю,
     * а также удаляет все подзадачи, связанные с этим эпиком.<br>
     *
     * @param id идентификатор эпика, который нужно удалить.
     */
    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            throw new NotFoundException("Невозможно удалить: эпик с id = " + id + " не найден");
        } else {
            historyManager.remove(id);
            epic.getSubtaskIDs().stream()
                    .map(subtasks::remove) // Удаляем подзадачи, связанные с эпиком
                    .filter(Objects::nonNull) // Фильтруем только существующие подзадачи
                    .filter(s -> s.getStartTime() != null) // Проверяем, что у подзадачи есть время начала
                    .forEach(prioritizedTasks::remove); // Удаляем подзадачи из приоритетной очереди
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
        List<Subtask> subs = epic.getSubtaskIDs().stream()
                .map(subtasks::get) // Получаем подзадачи по их ID
                .filter(Objects::nonNull) // Фильтруем только существующие подзадачи
                .collect(Collectors.toList()); // Собираем их в список
        if (subs.isEmpty()) {
            epic.setStatus(Status.NEW); // Если нет подзадач, устанавливаем статус NEW
            return;
        }
        boolean allNew = subs.stream().allMatch(s -> s.getStatus() == Status.NEW);  // Проверяем, все ли подзадачи новые
        boolean allDone = subs.stream().allMatch(s -> s.getStatus() == Status.DONE);    // Проверяем, все ли подзадачи выполнены
        epic.setStatus(allDone ? Status.DONE : allNew ? Status.NEW : Status.IN_PROGRESS);   // Устанавливаем статус эпика в зависимости от статусов подзадач
    }

    /**
     * Пересчитывает временные параметры эпика на основе его подзадач.
     * Если у эпика нет подзадач, устанавливает продолжительность в 0 и время начала/окончания в null.
     * Иначе суммирует продолжительности подзадач и определяет минимальное время начала и максимальное время окончания.
     *
     * @param epic Эпик, для которого нужно пересчитать временные параметры.
     */
    void recalculateEpicTimeDetails(Epic epic) {
        List<Subtask> subs = epic.getSubtaskIDs().stream()
                .map(subtasks::get) // Получаем подзадачи по их ID
                .filter(Objects::nonNull)   // Фильтруем только существующие подзадачи
                .collect(Collectors.toList());  // Собираем их в список
        if (subs.isEmpty()) {
            epic.setDuration(Duration.ZERO);    // Если нет подзадач, устанавливаем продолжительность в 0
            epic.setStartTime(null);    // Устанавливаем время начала в null
            epic.setEndTime(null);  // Устанавливаем время окончания в null
            return;
        }
        Duration total = subs.stream()
                .map(Subtask::getDuration)  // Суммируем продолжительности всех подзадач
                .reduce(Duration.ZERO, Duration::plus); // Начинаем с нулевой продолжительности и складываем
        LocalDateTime start = subs.stream()
                .map(Subtask::getStartTime) // Получаем время начала всех подзадач
                .filter(Objects::nonNull)   // Фильтруем только существующие времена начала
                .min(LocalDateTime::compareTo)  // Находим минимальное время начала
                .orElse(null);  // Если нет подзадач с временем начала, возвращаем null
        LocalDateTime end = subs.stream()
                .map(Subtask::getEndTime)   // Получаем время окончания всех подзадач
                .filter(Objects::nonNull)   // Фильтруем только существующие времена окончания
                .max(LocalDateTime::compareTo)  // Находим максимальное время окончания
                .orElse(null);  // Если нет подзадач с временем окончания, возвращаем null
        epic.setDuration(total);
        epic.setStartTime(start);
        epic.setEndTime(end);
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
        subtasks.values().forEach(s -> {
            historyManager.remove(s.getId());   // удаляем подзадачу из истории
            if (s.getStartTime() != null) prioritizedTasks.remove(s);   // удаляем из приоритетной очереди, если была
        });
        epics.values().forEach(epic -> {
            epic.clearSubtaskIds(); // Очищаем ID подзадач у эпика
            updateEpicStatus(epic); // Обновляем статус эпика после удаления всех подзадач
            recalculateEpicTimeDetails(epic);   // Пересчитываем временные параметры эпика
        });
        subtasks.clear();   // очищаем хранилище подзадач
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с id = " + id + " не найдена");
        } else {
            addToHistory(subtask);
        }
        return subtask;
    }

    /**
     * Добавляет новую подзадачу в менеджер.<br>
     * Генерирует уникальный идентификатор для подзадачи, проверяет на пересечение с другими подзадачами,
     * и если пересечений нет, добавляет подзадачу в приоритетную очередь и в хранилище подзадач.<br>
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
            prioritizedTasks.add(subtask); // Добавляем в приоритетную очередь
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
     *
     * @param subtask подзадача для обновления
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new NotFoundException("Невозможно обновить: подзадача с id = " + subtask.getId() + " не найдена");
        }
        Subtask old = subtasks.get(subtask.getId());
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old); // Удаляем старую версию из приоритетной очереди, если была
            }
            if (subtask.getStartTime() != null && hasIntersection(subtask)) {
                prioritizedTasks.add(old); // Возвращаем старую версию, если пересекается
                throw new ValidationException("Подзадача пересекается по времени с существующей");
            }
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask); // Добавляем новую версию в приоритетную очередь
            }
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик, к которому привязана подзадача
            if (epic != null) {
                updateEpicStatus(epic); // Обновляем статус эпика после обновления подзадачи
                recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
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
        if (removed == null) {
            throw new NotFoundException("Невозможно удалить: подзадача с id = " + id + " не найдена");
        } else if (removed.getStartTime() != null) {
            prioritizedTasks.remove(removed);
        } else {
            historyManager.remove(id); // Удаляем подзадачу из истории
            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id); // Удаляем подзадачу из эпика, если она была привязана к нему
                updateEpicStatus(epic); // Обновляем статус эпика после удаления подзадачи
                recalculateEpicTimeDetails(epic); // Пересчитываем временные параметры эпика
            }
        }
    }

    /**
     * Получает список подзадач, связанных с указанным эпиком.<br>
     *
     * @param epicId идентификатор эпика, для которого нужно получить подзадачи
     * @return Список подзадач, связанных с указанным эпиком.<br>
     */
    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        return Optional.ofNullable(epics.get(epicId))   // Получаем эпик по его ID
                .map(e -> e.getSubtaskIDs().stream()    // Получаем список ID подзадач эпика
                        .map(subtasks::get) // Получаем подзадачи по их ID
                        .filter(Objects::nonNull)   // Фильтруем только существующие подзадачи
                        .collect(Collectors.toList()))  // Собираем их в список
                .orElseGet(Collections::emptyList); // Если эпик не найден, возвращаем пустой список
    }
    //endregion
}
