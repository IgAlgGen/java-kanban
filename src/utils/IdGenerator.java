package utils;

public class IdGenerator {
    private static int idCounter = 1;

    private IdGenerator() {
    //Приватный конструктор для предотвращения создания экземпляров
    }

    public static int generateId() {
        return idCounter++;
    }
}
