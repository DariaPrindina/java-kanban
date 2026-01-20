package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksEqualIfSameId() {
        Task task1 = new Task("Task", "Desc");
        task1.setId(1);
        Task task2 = new Task("Another", "Another desc");
        task2.setId(1);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void tasksNotEqualIfDifferentId() {
        Task task1 = new Task("Task", "Desc");
        task1.setId(1);
        Task task2 = new Task("Another", "Another desc");
        task2.setId(2);

        assertNotEquals(task1, task2);
    }
}