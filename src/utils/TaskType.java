package utils;

public enum TaskType {
    TASK,
    EPIC,
    SUBTASK;

    // Метод для получения типа задачи из строки
    public static TaskType taskTypeFromString(String type) {
        try {
            return TaskType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}
