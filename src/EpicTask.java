import java.util.*;

public class EpicTask extends Task {
    private static int idCounter = 0;
    private final int epicId;
    private Progress epicProgress;
    HashMap<Integer, Subtask> subtasks = new HashMap<>(); // список задач, которые входят в эпик

    private EpicTask(int id, String name, String description, Progress progress, Subtask... subtasks) {
        super(id, name, description, progress);
        this.epicId = id;
        this.epicProgress = progress;
        for (Subtask subtask : subtasks) {
            this.subtasks.put(subtask.getSubtaskId(), subtask);
        }
    }

    private EpicTask(int id, String name, String description, Progress progress, HashMap<Integer, Subtask> subtasks) {
        super(id, name, description, progress);
        this.epicId = id;
        this.epicProgress = progress;
        this.subtasks = subtasks;
    }

    // Публичная фабрика
    public static EpicTask taskWithID(int id, String name, String description, Progress progress, Subtask... subtasks) {
        return new EpicTask(id, name, description, progress, subtasks);
    }

    public static EpicTask taskWithID(int id, String name, String description, Progress progress, HashMap<Integer, Subtask> subtasks) {
        return new EpicTask(id, name, description, progress, subtasks);
    }

    public EpicTask(String name, String description) {
        super(name, description);
        this.epicId = ++idCounter; // индивидуальный id
    }

    public EpicTask(String name, String description, Subtask... subtask) {
        super(name, description);
        this.epicId = ++idCounter; // индивидуальный id
        for (Subtask subtask1 : subtask) {
            this.subtasks.put(subtask1.getSubtaskId(), subtask1);
        }
    }

    public EpicTask(String name, String description, HashMap<Integer,Subtask> subtasks) {
        super(name, description);
        this.epicId = ++idCounter; // индивидуальный id
        this.subtasks = subtasks;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getDescription() {
        return super.getDescription();
    }

    public int getEpicId() {
        return epicId;
    }

    public Progress getEpicProgress() {
        return epicProgress;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EpicTask epicTask = (EpicTask) o;
        return epicId == epicTask.epicId && Objects.equals(subtasks, epicTask.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId, subtasks);
    }
}
