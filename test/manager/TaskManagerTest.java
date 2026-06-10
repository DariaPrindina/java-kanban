package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void addNewTaskAndGetIt() {
        Task task = new Task("Test task", "Test desc");
        manager.addTask(task);
        Task saved = manager.getTask(task.getId());

        assertNotNull(saved);
        assertEquals(task, saved);
        assertEquals(Status.NEW, saved.getStatus());
    }

    @Test
    void addNewEpicAndGetIt() {
        Epic epic = new Epic("Test epic", "Test desc");
        manager.addEpic(epic);
        Epic saved = manager.getEpic(epic.getId());

        assertNotNull(saved);
        assertEquals(epic, saved);
        assertEquals(Status.NEW, saved.getStatus());
    }

    @Test
    void addNewSubtaskAndGetIt() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        manager.addSubtask(subtask);

        Subtask saved = manager.getSubtask(subtask.getId());

        assertNotNull(saved);
        assertEquals(subtask, saved);
        assertEquals(epic.getId(), saved.getEpicId());
    }

    @Test
    void subtaskShouldHaveLinkToEpic() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        manager.addSubtask(subtask);

        Subtask saved = manager.getSubtask(subtask.getId());

        assertNotNull(saved);
        assertNotEquals(0, saved.getEpicId(), "Подзадача должна иметь ссылку на эпик");
        assertNotNull(manager.getEpic(saved.getEpicId()), "Связанный эпик должен существовать");
    }

    @Test
    void tasksWithGeneratedIdDontConflictWithManualId() {
        Task task1 = new Task("Task1", "Desc");
        task1.setId(100);
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc");
        manager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId());
        assertEquals(task1, manager.getTask(100));
    }

    @Test
    void taskUnchangedAfterAddingToManager() {
        Task original = new Task("Original", "Desc");
        original.setStatus(Status.IN_PROGRESS);
        original.setId(999);

        manager.addTask(original);
        Task saved = manager.getTask(original.getId());

        assertEquals(original.getName(), saved.getName());
        assertEquals(original.getDescription(), saved.getDescription());
        assertEquals(original.getStatus(), saved.getStatus());
        assertEquals(original.getId(), saved.getId());
    }

    @Test
    void changingOriginalTaskAfterAddingShouldNotAffectManagerData() {
        Task task = new Task("Task", "Desc");
        manager.addTask(task);

        task.setName("Changed");
        task.setStatus(Status.DONE);

        Task saved = manager.getTask(task.getId());

        assertEquals("Task", saved.getName());
        assertEquals(Status.NEW, saved.getStatus());
    }

    @Test
    void changingTaskAfterGettingShouldNotAffectManagerData() {
        Task task = new Task("Task", "Desc");
        manager.addTask(task);

        Task saved = manager.getTask(task.getId());
        saved.setName("Mutated");
        saved.setStatus(Status.DONE);

        Task updated = manager.getTask(task.getId());

        assertEquals("Task", updated.getName());
        assertEquals(Status.NEW, updated.getStatus());
    }

    @Test
    void historyContainsLastViewedTasks() {
        Task task1 = new Task("Task1", "Desc1");
        Task task2 = new Task("Task2", "Desc2");
        Epic epic1 = new Epic("Epic1", "DescE");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId());

        List<Task> history = manager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(epic1, history.get(2));
    }

    @Test
    void repeatedViewShouldKeepOnlyLastOccurrenceInHistory() {
        Task task1 = new Task("Task1", "Desc1");
        Task task2 = new Task("Task2", "Desc2");
        manager.addTask(task1);
        manager.addTask(task2);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task1.getId());

        List<Task> history = manager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task1.getId(), history.get(1).getId());
    }

    @Test
    void deleteTaskShouldRemoveTaskFromHistory() {
        Task task = new Task("Task", "Desc");
        manager.addTask(task);
        manager.getTask(task.getId());
        manager.deleteTask(task.getId());

        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void deleteSubtaskShouldRemoveIdFromEpic() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        manager.addSubtask(subtask);

        manager.deleteSubtask(subtask.getId());

        assertTrue(manager.getEpic(epic.getId()).getSubtaskIds().isEmpty());
    }

    @Test
    void deleteEpicShouldRemoveEpicAndSubtasksFromHistory() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("Sub1", "Desc1", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc2", epic.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        manager.getEpic(epic.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());
        manager.deleteEpic(epic.getId());

        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void deleteAllSubtasksShouldClearEpicSubtaskIds() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("Sub1", "Desc1", epic.getId()));
        manager.addSubtask(new Subtask("Sub2", "Desc2", epic.getId()));

        manager.deleteAllSubtasks();

        assertTrue(manager.getEpic(epic.getId()).getSubtaskIds().isEmpty());
    }

    @Test
    void getPrioritizedTasksReturnsSortedByStartTime() {
        Task task1 = new Task("Task1", "Desc");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        task1.setDuration(Duration.ofMinutes(30));

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        task2.setDuration(Duration.ofMinutes(30));

        manager.addTask(task1);
        manager.addTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId(), "Сначала должна идти более ранняя задача");
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }

    @Test
    void tasksWithoutStartTimeShouldNotBeInPrioritizedList() {
        Task taskWithTime = new Task("WithTime", "Desc");
        taskWithTime.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        taskWithTime.setDuration(Duration.ofMinutes(30));

        Task taskWithoutTime = new Task("WithoutTime", "Desc");

        manager.addTask(taskWithTime);
        manager.addTask(taskWithoutTime);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(1, prioritized.size());
        assertEquals(taskWithTime.getId(), prioritized.get(0).getId());
    }

    @Test
    void overlappingTasksShouldThrowException() {
        Task task1 = new Task("Task1", "Desc");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        task2.setDuration(Duration.ofMinutes(60));

        assertThrows(IllegalArgumentException.class, () -> manager.addTask(task2),
                "Добавление пересекающейся задачи должно вызвать исключение");
    }

    @Test
    void nonOverlappingTasksShouldNotThrowException() {
        Task task1 = new Task("Task1", "Desc");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        task2.setDuration(Duration.ofMinutes(60));

        assertDoesNotThrow(() -> manager.addTask(task2));
    }
}
