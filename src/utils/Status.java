package utils;

public enum Status {
    NEW,
    IN_PROGRESS,
    DONE;

    // Метод для получения статуса из строки
    public static Status statusFromString(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный статус задачи: " + status);
        }
    }
}