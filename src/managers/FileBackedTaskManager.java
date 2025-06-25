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
import java.time.Duration;
import java.time.LocalDateTime;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    // метод автосохранения
    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,startTime,duration,epic\n");

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
            reader.readLine(); // читаем и игнорируем заголовок
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue; // пропускаем пустые строки
                Task task = fromString(line);
                if (task instanceof Epic epic) {
                    manager.epics.put(epic.getId(), epic); // добавляем эпик в менеджер
                } else if (task instanceof Subtask subtask) {
                    manager.subtasks.put(subtask.getId(), subtask); // добавляем подзадачу в менеджер
                    manager.epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());// добавим подзадачу в эпик
                    manager.updateEpicStatus(manager.epics.get(subtask.getEpicId()));// обновляем статус эпика
                    manager.recalculateEpicTimeDetails(manager.epics.get(subtask.getEpicId()));// пересчитываем время эпика
                } else {
                    manager.tasks.put(task.getId(), task); // добавляем задачу в менеджер
                }
                IdGenerator.updateMaxId(task.getId()); // чтобы не повторялись ID
            }
            manager.prioritizeAll(); // обновляем приоритеты задач
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка загрузки из файла", e);
        }
        return manager;
    }

    private void prioritizeAll() {
        prioritizedTasks.clear();// Очищаем текущее множество
        for (Task t : getAllTasks()) {
            if (t.getStartTime() != null) {
                prioritizedTasks.add(t);// Добавляем только задачи и подзадачи с заданным startTime
            }
        }
        for (Subtask sub : getAllSubtasks()) {
            if (sub.getStartTime() != null) {
                prioritizedTasks.add(sub);
            }
        }
    }

    private static Task fromString(String line) {
        String[] fields = line.split(",");
        if (fields.length < 5 || fields.length > 8) {
            throw new IllegalArgumentException("Неверный формат строки: " + line);
        }

        // Преобразуем поля в соответствующие типы
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.taskTypeFromString(fields[1]);
        String name = fields[2];
        Status status = Status.statusFromString(fields[3]);
        String description = fields[4];
        String startTimeStr = fields[5].equals("null") ? null : fields[5];
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        String durationStr = fields[6];
        Long duration = durationStr != null? Long.parseLong(fields[6]) : 0L;



        return switch (type) {
            case TASK -> new Task(id, name, description, status, startTime, Duration.ofMinutes(duration));
            case EPIC -> new Epic(id, name, description, status, startTime, Duration.ofMinutes(duration));
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[7]);
                yield new Subtask(id, name, description, status, startTime,Duration.ofMinutes(duration), epicId);
            }
        };
    }

    private String toString(Task task) {
        String sb = task.getId() + "," +
                task.getType() + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                (task.getStartTime() != null ? task.getStartTime().toString() : "null") + "," +
                (task.getDuration() != null ? task.getDuration().toMinutes() : "0") + "," +
                (task instanceof Subtask ? ((Subtask) task).getEpicId() : "");
        return sb;
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
}
