import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Manager {
    // Возможность хранить задачи всех типов.
    Task task;
    EpicTask epicTask;
    Subtask subtask;
    // Хранит задачи по их ID
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, EpicTask> epicTasks = new HashMap<>();


    // Методы для каждого из типа задач (Задача/Эпик/Подзадача):
// a. Получение списка всех задач.
    public List<Task> getAllTasks() {
        return List.copyOf(tasks.values());
    }

    public List<EpicTask> getAllEpicTasks() {
        return List.copyOf(epicTasks.values());
    }

    public List<Subtask> getAllSubtasks() {
        List<Subtask> subtasks = new ArrayList<>();
        for (EpicTask epicTask : epicTasks.values()) {
            subtasks.addAll(epicTask.getSubtasks().values());
        }
        return subtasks;
    }

    // b. Удаление всех задач.
    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllEpicTasks() {
        epicTasks.clear();
    }

    public void removeAllSubtasks() {
        for (EpicTask epicTask : epicTasks.values()) {
            epicTask.getSubtasks().clear();
        }
    }

    // c. Получение по идентификатору.
    public Task getTaskById(int id) {
        if (tasks.get(id) == null) {
            System.out.println("Task с заданным ID " + id + " не найден.");
            return null; // Возвращаем null, или пробрасываем исключение
        }
        return tasks.get(id);
    }

    public EpicTask getEpicTaskById(int id) {
        if (epicTasks.get(id) == null) {
            System.out.println("EpicTask с заданным ID " + id + " не найден.");
            return null; // Возвращаем null, или пробрасываем исключение
        }
        return epicTasks.get(id);
    }

    public Subtask getSubtaskById(int id) {
        Subtask subtask = null;
        for (EpicTask epicTask : epicTasks.values()) {
            if (epicTask.getSubtasks().get(id) == null) {
                System.out.println("Subtask с заданным ID " + id + " не найден.");
                subtask = null; // Возвращаем null, или пробрасываем исключение
            }
        }
        for (EpicTask epicTask : epicTasks.values()) {
            if (epicTask.getSubtasks().containsKey(id)) {
                subtask = epicTask.getSubtasks().get(id);
            }
        }
        return subtask;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void addEpicTask(EpicTask epicTask) {
        epicTasks.put(epicTask.getEpicId(), epicTask);
    }

    public void addSubtask(Subtask subtask, int epicId) {
        EpicTask epicTask = epicTasks.get(epicId);
        if (epicTask != null) {
            epicTask.getSubtasks().put(subtask.getSubtaskId(), subtask);
        } else {
            System.out.println("EpicTask с заданным ID " + epicId + " не найден."); // или пробрасываем исключение
        }
    }

// e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Task с заданным ID " + task.getId() + " не найден.");
        }
    }

    public void updateEpicTask(EpicTask epicTask) {
        if (epicTasks.containsKey(epicTask.getEpicId())) {
            epicTasks.put(epicTask.getEpicId(), epicTask);
        } else {
            System.out.println("EpicTask с заданным ID " + epicTask.getEpicId() + " не найден.");
        }
    }

    public void updateSubtask(Subtask subtask) {
        for (EpicTask epicTask : epicTasks.values()) {
            if (epicTask.getSubtasks().containsKey(subtask.getSubtaskId())) {
                epicTask.getSubtasks().put(subtask.getSubtaskId(), subtask);
                return;
            }
        }
        System.out.println("Subtask с заданным ID " + subtask.getSubtaskId() + " не найден.");
    }
// f. Удаление по идентификатору.
    public void removeTaskById(int id) {
        if (tasks.remove(id) == null) {
            System.out.println("Task с заданным ID " + id + " не найден.");
        }
    }

    public void removeEpicTaskById(int id) {
        if (epicTasks.remove(id) == null) {
            System.out.println("EpicTask с заданным ID " + id + " не найден.");
        }
    }

    public void removeSubtaskById(int id) {
        for (EpicTask epicTask : epicTasks.values()) {
            if (epicTask.getSubtasks().remove(id) == null) {
                System.out.println("Subtask с заданным ID " + id + " не найден.");
            }
        }
    }
}
