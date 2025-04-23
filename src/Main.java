
public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description 1");
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask Description 2");
        Subtask subtask3 = new Subtask("Subtask 3", "Subtask Description 3");
        Subtask subtask4 = new Subtask("Subtask 4", "Subtask Description 4");

        EpicTask epicTask1 = new EpicTask("Epic Task 1", "Epic Task Description 1", subtask1, subtask2);
        EpicTask epicTask2 = new EpicTask("Epic Task 2", "Epic Task Description 2", subtask3, subtask4);

        Task task3 = new Task("Task 3", "Description 3");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpicTask(epicTask1);
        manager.addEpicTask(epicTask2);
        manager.addTask(task3);


        System.out.println("All tasks:");
        for (Task task : manager.getAllTasks()) {
            System.out.println("ID: " + task.getId() + ", Name: " + task.getName() + ", Description: " + task.getDescription());
        }
        System.out.println("\nAll epic tasks:");
        for (EpicTask epicTask : manager.getAllEpicTasks()) {
            System.out.println("ID: " + epicTask.getEpicId() + ", Name: " + epicTask.getName() + ", Description: " + epicTask.getDescription());
            System.out.println("Subtasks:");
            for (Subtask subtask : epicTask.getSubtasks().values()) {
                System.out.println("  ID: " + subtask.getSubtaskId() + ", Name: " + subtask.getName() + ", Description: " + subtask.getDescription());
            }
        }
        System.out.println("\nAll subtasks:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println("ID: " + subtask.getSubtaskId() + ", Name: " + subtask.getName() + ", Description: " + subtask.getDescription());
        }


    }
}
