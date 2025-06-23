import managers.FileBackedTaskManager;
import managers.HistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;
import utils.Status;
import managers.TaskManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        Path filePath = Path.of("tasks.csv");
        TaskManager manager = FileBackedTaskManager.loadFromFile(filePath.toFile());
        //preparingForFirstStart(manager);

//        BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath.toFile()));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            String[] parts = line.split(",");
//            System.out.println(parts.length + " parts: " + line);
//        }



        System.out.println("All tasks:");
        printList(manager.getAllTasks());
        System.out.println("\nAll epics:");
        printList(manager.getAllEpics());
        System.out.println("\nAll subtasks:");
        printList(manager.getAllSubtasks());





    }

    private static void preparingForFirstStart(TaskManager manager) {
        System.out.println("_" + "_".repeat(20) + "Test TaskManager" + "_" + "_".repeat(20));
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


    private static void printList(List<? extends Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Список пуст");
        } else {
            for (Task task : tasks) {
                if (task == null) {
                    System.out.println("null");
                } else {
                    System.out.print(task.getClass().getSimpleName() + " ID: " + task.getId() +
                            ", Name: " + task.getName() +
                            ", Description: " + task.getDescription() +
                            ", Status: " + task.getStatus());
                    if (task.getStartTime() != null) {
                        System.out.print(", Start Time: " + task.getStartTime() +
                                ", Duration: " + task.getDuration());
                        System.out.println();

                    }
                    if (task instanceof Epic) {
                        Epic epic = (Epic) task;
                        System.out.print("  Subtask IDs: " + epic.getSubtaskIDs() );
                        if (epic.getEndTime() != null) {
                            System.out.print(", End Time: " + epic.getEndTime());
                            System.out.println();
                        }
                    } else if (task instanceof Subtask) {
                        Subtask subtask = (Subtask) task;
                        System.out.print("  Epic ID: " + subtask.getEpicId());
                        System.out.println();
                    }
                }
            }
        }
    }
}