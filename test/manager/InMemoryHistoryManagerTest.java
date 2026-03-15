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
    void historyShouldStoreMoreThanTenTasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task" + i, "Desc" + i);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(15, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(15, history.get(14).getId());
    }

    @Test
    void shouldKeepOnlyLastViewOfTask() {
        Task task1 = new Task("Task1", "Desc1");
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc2");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(1, history.get(1).getId());
    }

    @Test
    void shouldRemoveTaskFromHistoryById() {
        Task task1 = new Task("Task1", "Desc1");
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc2");
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    void shouldRemoveFirstTaskFromHistory() {
        Task task1 = new Task("Task1", "Desc1");
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc2");
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    void shouldRemoveLastTaskFromHistory() {
        Task task1 = new Task("Task1", "Desc1");
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc2");
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
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
