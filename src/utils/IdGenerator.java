package utils;

public final class IdGenerator {
    private static int idCounter = 1;

    private IdGenerator() {
    //Приватный конструктор для предотвращения создания экземпляров
    }

    public static int generateId() {
        return idCounter++;
    }

    public static void updateMaxId(int maxId) {
        if (maxId >= idCounter) {
            idCounter = maxId + 1;
        }
    }
}
