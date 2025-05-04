package utils;

public class IdGenerator {
    private static int idCounter = 1;

    public static int generateId() {
        return idCounter++;
    }
}
