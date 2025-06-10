import managers.FileBackedTaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;
import utils.Status;
import managers.TaskManager;

import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Path filePath = Path.of("tasks.csv");

        TaskManager manager = FileBackedTaskManager.loadFromFile(filePath.toFile());


//        System.out.println("_" + "_".repeat(20) + "Test TaskManager" + "_" + "_".repeat(20));
//        manager.addTask(new Task("Task 1", "Description 1", Status.NEW));
//        manager.addTask(new Task("Task 2", "Description 2", Status.NEW));
//        manager.addEpic(new Epic("Epic 1", "Description 1", Status.NEW));
//        manager.addEpic(new Epic("Epic 2", "Description 2", Status.NEW));
//        manager.addSubtask(new Subtask("Subtask 1", "Description 1", Status.NEW, 4));
//        manager.addSubtask(new Subtask("Subtask 2", "Description 2", Status.IN_PROGRESS, 4));
//        manager.addSubtask(new Subtask("Subtask 3", "Description 3", Status.NEW, 4));

        printList(manager.getAllTasks());
        printList(manager.getAllEpics());
        printList(manager.getAllSubtasks());

//        manager.getTaskById(1);
//        manager.getEpicById(3);
//        manager.getEpicById(4);
//        manager.getSubtaskById(5);
//        manager.getSubtaskById(6);

        //printList(manager.getFromHistory());

//        System.out.println("_" + "_".repeat(20) + "Test History" + "_" + "_".repeat(20));
//        manager.getEpicById(3);
//        manager.removeSubtaskById(5);
//        printList(manager.getFromHistory());

//        System.out.println("_" + "_".repeat(20) + "Test History" + "_" + "_".repeat(20));
//        manager.removeEpicById(4);
//        manager.removeTaskById(1);
//        printList(manager.getFromHistory());

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