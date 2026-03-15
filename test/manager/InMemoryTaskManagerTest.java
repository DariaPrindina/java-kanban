package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void addNewTaskAndGetIt() {
        Task task = new Task("Test task", "Test desc");
        manager.addTask(task);
        Task saved = manager.getTask(task.getId());

        assertNotNull(saved, "Задача не найдена");
        assertEquals(task, saved, "Задачи не совпадают");
        assertEquals(Status.NEW, saved.getStatus(), "Статус не NEW");
    }

    @Test
    void addNewEpicAndGetIt() {
        Epic epic = new Epic("Test epic", "Test desc");
        manager.addEpic(epic);
        Epic saved = manager.getEpic(epic.getId());

        assertNotNull(saved, "Эпик не найден");
        assertEquals(epic, saved, "Эпики не совпадают");
        assertEquals(Status.NEW, saved.getStatus(), "Статус не NEW");
    }

    @Test
    void addNewSubtaskAndGetIt() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        manager.addSubtask(subtask);

        Subtask saved = manager.getSubtask(subtask.getId());

        assertNotNull(saved, "Подзадача не найдена");
        assertEquals(subtask, saved, "Подзадачи не совпадают");
        assertEquals(epic.getId(), saved.getEpicId(), "Эпик ID не совпадает");
    }

    @Test
    void tasksWithGeneratedIdDontConflictWithManualId() {
        Task task1 = new Task("Task1", "Desc");
        task1.setId(100);
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc");
        manager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID конфликтуют");
        assertEquals(task1, manager.getTask(100), "Задача с ручным ID не найдена");
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
        task.setDescription("Changed desc");
        task.setStatus(Status.DONE);

        Task saved = manager.getTask(task.getId());

        assertEquals("Task", saved.getName());
        assertEquals("Desc", saved.getDescription());
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

        assertEquals(3, history.size(), "Неверное количество в истории");
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

        assertEquals(2, history.size(), "В истории не должно быть дублей");
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task1.getId(), history.get(1).getId());
    }

    @Test
    void deleteTaskShouldRemoveTaskFromHistory() {
        Task task = new Task("Task", "Desc");
        manager.addTask(task);

        manager.getTask(task.getId());
        manager.deleteTask(task.getId());

        assertTrue(manager.getHistory().isEmpty(), "Удалённая задача должна исчезнуть из истории");
    }

    @Test
    void deleteSubtaskShouldRemoveIdFromEpic() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());
        manager.addSubtask(subtask);

        manager.deleteSubtask(subtask.getId());

        Epic savedEpic = manager.getEpic(epic.getId());
        assertTrue(savedEpic.getSubtaskIds().isEmpty(), "У эпика не должно оставаться старых id подзадач");
    }

    @Test
    void deleteEpicShouldRemoveEpicAndSubtasksFromHistory() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask1", "Desc1", epic.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Desc2", epic.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        manager.getEpic(epic.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());
        manager.deleteEpic(epic.getId());

        assertTrue(manager.getHistory().isEmpty(),
                "После удаления эпика в истории не должно остаться ни эпика, ни его подзадач");
    }

    @Test
    void deleteAllSubtasksShouldClearEpicSubtaskIds() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("Subtask1", "Desc1", epic.getId()));
        manager.addSubtask(new Subtask("Subtask2", "Desc2", epic.getId()));

        manager.deleteAllSubtasks();

        Epic savedEpic = manager.getEpic(epic.getId());
        assertTrue(savedEpic.getSubtaskIds().isEmpty(),
                "После удаления всех подзадач у эпика не должно остаться неактуальных id");
    }
}
