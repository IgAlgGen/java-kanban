import java.util.Objects;

public class Task {
    private static int idCounter = 0; // общий счётчик
    private final int id;             // индивидуальный id
    private String name;
    private String description;
    private Progress progress;

    // Приватный конструктор с ручным ID
    protected Task(int id, String name, String description, Progress progress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.progress = progress;
    }

    // Публичная фабрика
    public static Task taskWithID(int id, String name, String description, Progress progress) {
        return new Task(id, name, description, progress);
    }

    public Task(String name, String description) {
        this.id = ++idCounter;
        this.name = name;
        this.description = description;
        this.progress = Progress.NEW;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description) && progress == task.progress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, progress);
    }
}
