package managers;

import exeptions.NotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    //region model.Task
    List<Task> getAllTasks();

    void removeAllTasks();

    Task getTaskById(int id) throws NotFoundException;

    void addTask(Task task);

    void updateTask(Task task) throws NotFoundException;

    void removeTaskById(int id) throws NotFoundException;
    //endregion

    //region model.Epic
    List<Epic> getAllEpics();

    void removeAllEpics();

    Epic getEpicById(int id) throws NotFoundException;

    void addEpic(Epic epic);

    void updateEpic(Epic epic) throws NotFoundException;

    void removeEpicById(int id) throws NotFoundException;
    //endregion

    //region model.Subtask
    List<Subtask> getAllSubtasks();

    void removeAllSubtasks();

    Subtask getSubtaskById(int id) throws NotFoundException;

    void addSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask) throws NotFoundException;

    void removeSubtaskById(int id) throws NotFoundException;

    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getPrioritizedTasks();

    //endregion
    List<Task> getFromHistory();
}