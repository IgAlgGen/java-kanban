import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        testTask(manager);
        testEpic(manager);
        testSubtask(manager);

    }

    private static void testEpic(TaskManager manager) {
        System.out.println("_".repeat(20) + "Test Epics" + "_".repeat(20));
        Epic epic1 = new Epic(3, "Epic 3", "Description 3", Status.NEW);
        Epic epic2 = new Epic(3, "Epic 3", "Description 3", Status.NEW);
        manager.addEpic(new Epic(3, "Epic 3", "Description 3", Status.NEW));
        System.out.println("проверка по Equals");
        System.out.println(epic1.equals(epic2));
        System.out.println((manager.getEpicById(3)).equals(epic1));
        System.out.println("проверка по HashCode");
        System.out.println(epic1.hashCode() == epic2.hashCode());
        System.out.println((manager.getEpicById(3)).hashCode() == epic1.hashCode());

        System.out.println("проверка getAllEpics");
        manager.addEpic(epic2);
        manager.addEpic(new Epic(5, "Epic 5", "Description 5", Status.NEW));
        printList(manager.getAllEpics());

        System.out.println("проверка getEpicById");
        System.out.println(manager.getEpicById(3));

        System.out.println("проверка updateEpic");
        manager.updateEpic(new Epic(3, "Updated Epic 3", "Updated Description 3", Status.IN_PROGRESS));
        printList(manager.getAllEpics());

        System.out.println("проверка removeEpicById");
        manager.removeEpicById(3);
        printList(manager.getAllEpics());

    }

    private static void testSubtask(TaskManager manager) {
        System.out.println("_".repeat(20) + "Test Subtasks" + "_".repeat(20));
        Subtask subtask1 = new Subtask(6, "Subtask 6", "Description 6", Status.NEW, 5);
        Subtask subtask2 = new Subtask(6, "Subtask 6", "Description 6", Status.NEW, 5);
        manager.addSubtask(new Subtask(0, "Subtask 6", "Description 6", Status.NEW, 5));
        System.out.println("проверка по Equals");
        System.out.println(subtask1.equals(subtask2));
        System.out.println((manager.getSubtaskById(6)).equals(subtask1));
        System.out.println("проверка по HashCode");
        System.out.println(subtask1.hashCode() == subtask2.hashCode());
        System.out.println((manager.getSubtaskById(6)).hashCode() == subtask1.hashCode());

        System.out.println("проверка getAllSubtasks");
        manager.addSubtask(new Subtask(7, "Subtask 7", "Description 7", Status.IN_PROGRESS, 5));
        manager.addSubtask(new Subtask(8, "Subtask 8", "Description 8", Status.DONE, 4));
        manager.addSubtask(new Subtask(9, "Subtask 9", "Description 9", Status.DONE, 4));
        manager.addSubtask(new Subtask(10, "Subtask 10", "Description 10", Status.NEW, 5));
        manager.addSubtask(new Subtask(11, "Subtask 11", "Description 11", Status.NEW, 5));
        printList(manager.getAllSubtasks());

        System.out.println("проверка getSubtaskById");
        System.out.println(manager.getSubtaskById(6));

        System.out.println("проверка updateSubtask");
        manager.updateSubtask(new Subtask(6, "Updated Subtask 6", "Updated Description 6", Status.IN_PROGRESS, 1));
        printList(manager.getAllSubtasks());

        System.out.println("проверка removeSubtaskById");
        manager.removeSubtaskById(6);
        printList(manager.getAllSubtasks());

        System.out.println("проверка getSubtasksOfEpic");
        System.out.println("Epic ID: 5");
        System.out.println(manager.getEpicById(5));
        System.out.println("Subtasks из Epic ID 5:");
        printList(manager.getSubtasksOfEpic(5));
        System.out.println("Epic ID: 4");
        System.out.println(manager.getEpicById(4));
        System.out.println("Subtasks из Epic ID 4:");
        printList(manager.getSubtasksOfEpic(4));
    }

    private static void testTask(TaskManager manager) {
        System.out.println("_".repeat(20) + "Test Tasks" + "_".repeat(20));
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(1, "Task 1", "Description 1", Status.NEW);
        manager.addTask(new Task(1, "Task 1", "Description 1", Status.NEW));

        System.out.println("проверка по Equals");
        System.out.println(task1.equals(task2));
        System.out.println((manager.getTaskById(1)).equals(task1));

        System.out.println("проверка по HashCode");
        System.out.println(task1.hashCode() == task2.hashCode());
        System.out.println((manager.getTaskById(1)).hashCode() == task1.hashCode());

        System.out.println("проверка getAllTasks");
        manager.addTask(new Task(2, "Task 2", "Description 2", Status.NEW));
        printList(manager.getAllTasks());

        System.out.println("проверка updateTask");
        manager.updateTask(new Task(1, "Updated Task 1", "Updated Description 1", Status.IN_PROGRESS));
        manager.updateTask(new Task(2, "Updated Task 2", "Updated Description 2", Status.DONE));
        printList(manager.getAllTasks());

        System.out.println("проверка getTaskById");
        System.out.println(manager.getTaskById(1));

        System.out.println("проверка removeTaskById");
        manager.removeTaskById(1);
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