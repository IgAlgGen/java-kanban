package managers;

public class InMemoryTaskManagerTest extends AbstractTaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}
