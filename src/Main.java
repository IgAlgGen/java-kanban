import managers.FileBackedTaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Status;
import managers.TaskManager;

import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Path filePath = Path.of("tasks.csv");

        TaskManager manager = FileBackedTaskManager.loadFromFile(filePath.toFile());

        preparingForFirstStart(manager);

        printList(manager.getAllTasks());
        printList(manager.getAllEpics());
        printList(manager.getAllSubtasks());

        System.out.println("_" + "_".repeat(20) + "Test TaskManager" + "_" + "_".repeat(20));
        manager.addTask(new Task("Task 1", "Description 1", Status.DONE));
        manager.addTask(new Task("Task 2", "Description 2", Status.DONE));
        manager.addEpic(new Epic("Epic 1", "Description 1", Status.DONE));

        printList(manager.getAllTasks());
        printList(manager.getAllEpics());
        printList(manager.getAllSubtasks());
    }

    private static void preparingForFirstStart(TaskManager manager) {
        System.out.println("_" + "_".repeat(20) + "Test TaskManager" + "_" + "_".repeat(20));
        manager.addTask(new Task("Task 1", "Description 1", Status.NEW));
        manager.addTask(new Task("Task 2", "Description 2", Status.NEW));
        manager.addEpic(new Epic("Epic 1", "Description 1", Status.NEW));
        manager.addEpic(new Epic("Epic 2", "Description 2", Status.NEW));
        manager.addSubtask(new Subtask("Subtask 1", "Description 1", Status.NEW, 4));
        manager.addSubtask(new Subtask("Subtask 2", "Description 2", Status.IN_PROGRESS, 4));
        manager.addSubtask(new Subtask("Subtask 3", "Description 3", Status.NEW, 4));
    }

    private static void printList(List<? extends Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Список пуст");
        } else {
            for (Task task : tasks) {
                if (task == null) {
                    System.out.println("null");
                } else {
                    System.out.println(task.getClass().getSimpleName() + " ID: " + task.getId() +
                            ", Name: " + task.getName() +
                            ", Description: " + task.getDescription() +
                            ", Status: " + task.getStatus());
                }
            }
        }
    }
}