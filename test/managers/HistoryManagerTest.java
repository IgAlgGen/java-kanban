package managers;

import model.Task;
import org.junit.jupiter.api.*;
import utils.Managers;
import utils.Status;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    private HistoryManager history;

    @BeforeEach
    void setUp() {
        history = Managers.getDefaultHistory();
    }

    @Test
    void emptyHistory() {
        assertTrue(history.getHistory().isEmpty(), "История должна быть пустой при инициализации");
    }

    @Test
    void duplicateIgnored() {
        Task t = new Task(0, "H1", "HD1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        history.add(t);
        history.add(t);
        assertEquals(1, history.getHistory().size(), "Повторное добавление не должно создавать дубликаты");
    }

    @Test
    void removeFromStart() {
        Task t1 = new Task(0, "H1", "", Status.NEW, LocalDateTime.now(), Duration.ZERO);
        Task t2 = new Task(0, "H2", "", Status.NEW, LocalDateTime.now(), Duration.ZERO);
        history.add(t1);
        history.add(t2);
        history.remove(t1.getId());
        assertFalse(history.getHistory().contains(t1), "Первый элемент должен быть удален");
        assertEquals(1, history.getHistory().size());
    }

    @Test
    void removeFromMiddle() {
        Task t1 = new Task(0, "H1", "", Status.NEW, LocalDateTime.now(), Duration.ZERO);
        Task t2 = new Task(0, "H2", "", Status.NEW, LocalDateTime.now(), Duration.ZERO);
        Task t3 = new Task(0, "H3", "", Status.NEW, LocalDateTime.now(), Duration.ZERO);
        history.add(t1);
        history.add(t2);
        history.add(t3);
        history.remove(t2.getId());
        assertFalse(history.getHistory().contains(t2), "Средний элемент должен быть удален");
        assertEquals(2, history.getHistory().size());
    }

    @Test
    void removeFromEnd() {
        Task t1 = new Task(0, "H1", "", Status.NEW, LocalDateTime.now(), Duration.ZERO);
        Task t2 = new Task(0, "H2", "", Status.NEW, LocalDateTime.now(), Duration.ZERO);
        history.add(t1);
        history.add(t2);
        history.remove(t2.getId());
        assertFalse(history.getHistory().contains(t2), "Последний элемент должен быть удален");
        assertEquals(1, history.getHistory().size());
    }
}