import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history;

    public InMemoryHistoryManager() {
        this.history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            if (history.size() > 10) {
                history.removeFirst();
            }
            history.add(task);

        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
