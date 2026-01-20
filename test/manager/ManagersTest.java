package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
        assertTrue(manager instanceof InMemoryTaskManager);
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history);
        assertTrue(history instanceof InMemoryHistoryManager);
    }
}