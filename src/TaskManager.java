import java.util.List;

public interface TaskManager {
    //region Task
    List<Task> getAllTasks();

    void removeAllTasks();

    Task getTaskById(int id);

    void addTask(Task task);

    void updateTask(Task task);

    void removeTaskById(int id);
    //endregion

    //region Epic
    List<Epic> getAllEpics();

    void removeAllEpics();

    Epic getEpicById(int id);

    void addEpic(Epic epic);

    void updateEpic(Epic epic);

    void removeEpicById(int id);
    //endregion

    //region Subtask
    List<Subtask> getAllSubtasks();

    void removeAllSubtasks();

    Subtask getSubtaskById(int id);

    void addSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void removeSubtaskById(int id);

    List<Subtask> getSubtasksOfEpic(int epicId);

    //endregion
    List<Task> getFromHistory();
}