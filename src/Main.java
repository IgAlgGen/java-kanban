import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;
import utils.Status;
import managers.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        manager.addTask(new Task("Task 1", "Description 1", Status.NEW, LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofMinutes(90)));

//        manager.addTask(new Task("Task 2", "Description 2", Status.NEW));
//        manager.addEpic(new Epic("Epic 1", "Description 1", Status.NEW));
//        manager.addEpic(new Epic("Epic 2", "Description 2", Status.NEW));
//        manager.addSubtask(new Subtask("Subtask 1", "Description 1", Status.NEW, 4));
//        manager.addSubtask(new Subtask("Subtask 2", "Description 2", Status.NEW, 4));
//        manager.addSubtask(new Subtask("Subtask 3", "Description 3", Status.NEW, 4));

        System.out.println("All tasks:");
        printList(manager.getAllTasks());
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