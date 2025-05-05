import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;
import utils.Status;
import managers.TaskManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        manager.addTask(new Task("Task 1", "Description 1", Status.NEW));
        manager.addTask(new Task("Task 2", "Description 2", Status.NEW));
        manager.addEpic(new Epic("Epic 1", "Description 1", Status.NEW));
        manager.addSubtask(new Subtask("Subtask 1", "Description 1", Status.DONE, 3));
        manager.addSubtask(new Subtask("Subtask 2", "Description 2", Status.DONE, 3));
        manager.addSubtask(new Subtask("Subtask 3", "Description 3", Status.DONE, 3));

        manager.updateTask(new Task(1, "Updated Task 1", "Updated Description 1", Status.IN_PROGRESS));
        manager.addTask(new Task(1, "Updated Task 1", "Updated Description 1", Status.IN_PROGRESS));

        System.out.println(manager.getTaskById(1));
        System.out.println(manager.getEpicById(3));
        System.out.println(manager.getSubtaskById(6));

        printList(manager.getAllTasks());
        printList(manager.getAllEpics());
        printList(manager.getAllSubtasks());
        printList(manager.getSubtasksOfEpic(3));

        System.out.println("_".repeat(20) + "Test History" + "_".repeat(20));
        printList(manager.getFromHistory());
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
                            ", utils.Status: " + task.getStatus());
                }
            }
        }
    }
}