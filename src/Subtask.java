import java.util.Objects;

public class Subtask extends Task{
    private static int idCounter = 0;
    private final int subtaskId;
    private Progress subtaskProgress;

    public Subtask(String name, String description) {
        super(name, description);
        this.subtaskId = ++idCounter; // индивидуальный id
    }


    public int getSubtaskId() {
        return subtaskId;
    }

    public Progress getSubtaskProgress() {
        return subtaskProgress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return subtaskId == subtask.subtaskId && subtaskProgress == subtask.subtaskProgress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskId);
    }
}
