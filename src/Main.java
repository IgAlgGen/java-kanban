import managers.FileBackedTaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Status;
import managers.TaskManager;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        Path filePath = Path.of("tasks.csv");
        TaskManager manager = FileBackedTaskManager.loadFromFile(filePath.toFile());

        manager.getPrioritizedTasks().forEach(task -> {
            System.out.println(task.getClass().getSimpleName() + " ID: " + task.getId() +
                    ", Name: " + task.getName() +
                    ", Start Time: " + task.getStartTime() +
                    ", Duration: " + task.getDuration());
        });

        System.out.println("All tasks:");
        printList(manager.getAllTasks());
        System.out.println("\nAll epics:");
        printList(manager.getAllEpics());
        System.out.println("\nAll subtasks:");
        printList(manager.getAllSubtasks());


    }

    private static void preparingForFirstStart(TaskManager manager) {
        System.out.println("!" + "_".repeat(20) + "Test TaskManager" + "_" + "!".repeat(20));
        System.out.println("Создание менеджера задач");
        System.out.println("Добавление задач в менеджер");
        manager.addTask(new Task("Task 1", "Description 1", Status.NEW, LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofMinutes(90)));
        manager.addTask(new Task("Task 2", "Description 2", Status.NEW, LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(60)));
        manager.addEpic(new Epic("Epic 1", "Description 1", Status.NEW, Duration.ofMinutes(120), LocalDateTime.of(2025, 1, 1, 14, 0)));
        manager.addEpic(new Epic("Epic 2", "Description 2", Status.NEW, Duration.ofMinutes(150), LocalDateTime.of(2025, 1, 1, 16, 0)));
        manager.addSubtask(new Subtask("Subtask 1", "Description 1", Status.NEW, LocalDateTime.of(2025, 1, 1, 18, 0), Duration.ofMinutes(30), 3));
        manager.addSubtask(new Subtask("Subtask 2", "Description 2", Status.NEW, LocalDateTime.of(2025, 1, 1, 19, 0), Duration.ofMinutes(45), 4));
        manager.addSubtask(new Subtask("Subtask 3", "Description 3", Status.NEW, LocalDateTime.of(2025, 1, 1, 20, 0), Duration.ofMinutes(60), 4));
        System.out.println("Задачи добавлены в менеджер");

    }

    /**
     * Печатает список задач, эпиков и подзадач.
     *
     * @param tasks список задач для печати
     */
    private static void printList(List<? extends Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("Список пуст");
            return;
        }

        tasks.forEach(task -> {
            if (task == null) {
                System.out.println("null");
            } else {
                System.out.println(buildTaskLine(task));

                if (task instanceof Epic epic) {
                    System.out.println("  Subtask IDs: " + epic.getSubtaskIDs() +
                            optionalField(epic.getEndTime(), ", End Time: %s"));
                } else if (task instanceof Subtask subtask) {
                    System.out.println("  Epic ID: " + subtask.getEpicId());
                }
            }
        });
    }

    /**
     * Строит строку для вывода информации о задаче.
     *
     * @param task задача, для которой строится строка
     * @return строка с информацией о задаче
     */
    private static String buildTaskLine(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getClass().getSimpleName())
                .append(" ID: ").append(task.getId())
                .append(", Name: ").append(task.getName())
                .append(", Description: ").append(task.getDescription())
                .append(", Status: ").append(task.getStatus());

        sb.append(optionalField(task.getStartTime(), ", Start Time: %s"));
        sb.append(optionalField(task.getDuration(), ", Duration: %s"));
        return sb.toString();
    }

    /**
     * Возвращает строку с опциональным полем, если значение не null.
     *
     * @param value  значение, которое нужно проверить
     * @param format формат строки, если значение не null
     * @return строка с отформатированным значением или пустая строка
     */
    private static String optionalField(Object value, String format) {
        return value != null ? String.format(format, value) : "";
    }
}