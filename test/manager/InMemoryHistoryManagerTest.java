package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void addTaskToHistory() {
        Task task = new Task("Test", "Desc");
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task, historyManager.getHistory().get(0));
    }

    @Test
    void historyLimitedTo10Tasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task" + i, "Desc" + i);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(10, history.size(), "История должна содержать ровно 10 задач");
        assertEquals(6, history.get(0).getId(), "Первый элемент — 6-й");
        assertEquals(15, history.get(9).getId(), "Последний элемент — 15-й");
    }

    @Test
    void historyReturnsCopy() {
        Task task = new Task("Test", "Desc");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        history.clear();

        assertEquals(1, historyManager.getHistory().size(), "Оригинал не должен измениться");
    }
}