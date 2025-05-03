public class Managers {
    private Managers() {
        // Частный конструктор для предотвращения экземпляров
    }
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
