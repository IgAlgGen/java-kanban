package utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {
    private static final AtomicInteger nextId = new AtomicInteger(1);

    private IdGenerator() {
    //Приватный конструктор для предотвращения создания экземпляров
    }

    public static int generateId() {
        return nextId.getAndIncrement();
    }

    public static void updateMaxId(int maxId) {
        if (maxId >= nextId.get()) {
            nextId .set(maxId + 1);
        }
    }

    public static void reset() {
        nextId.set(1);
    }
}
