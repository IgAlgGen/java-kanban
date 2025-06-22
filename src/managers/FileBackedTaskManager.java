package managers;

import exeptions.ManagerLoadException;
import exeptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.IdGenerator;
import utils.Status;
import utils.TaskType;

import java.io.*;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    // метод автосохранения
    protected void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,startTime,duration\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

            writer.newLine();

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("id,type,name,status,description,epic")) {
                    continue; // пропускаем заголовок и пустые строки
                }
                Task task = fromString(line);
                if (task instanceof Epic epic) {
                    manager.epics.put(epic.getId(), epic);
                } else if (task instanceof Subtask subtask) {
                    manager.subtasks.put(subtask.getId(), subtask);
                    manager.epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());// добавим подзадачу в эпик
                    manager.updateEpicStatus(manager.epics.get(subtask.getEpicId()));// обновляем статус эпика
                } else {
                    manager.tasks.put(task.getId(), task);
                }
                IdGenerator.updateMaxId(task.getId()); // чтобы не повторялись ID

            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка загрузки из файла", e);
        }
        return manager;
    }

    private static Task fromString(String line) {
        String[] fields = line.split(",");

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.taskTypeFromString(fields[1]);
        String name = fields[2];
        Status status = Status.statusFromString(fields[3]);
        String description = fields[4];

        return switch (type) {
            case TASK -> new Task(id, name, description, status);
            case EPIC -> new Epic(id, name, description, status);
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[5]);
                yield new Subtask(id, name, description, status, epicId);
            }
        };
    }

    //region Методы переопределенные из InMemoryTaskManager
    @Override
    public void addTask(Task task) {
        super.addTask(task);
        saveToFile();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        saveToFile();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        saveToFile();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        saveToFile();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        saveToFile();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        saveToFile();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        saveToFile();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        saveToFile();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        saveToFile();
    }
    //endregion

    private String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        }
        return sb.toString();
    }

}
