package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void saveAndLoadEmptyManager() throws IOException {
        File file = File.createTempFile("kanban_empty", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getAllTasks().isEmpty(), "Задач не должно быть");
        assertTrue(loaded.getAllEpics().isEmpty(), "Эпиков не должно быть");
        assertTrue(loaded.getAllSubtasks().isEmpty(), "Подзадач не должно быть");
    }

    @Test
    void saveAndLoadSeveralTasks() throws IOException {
        File file = File.createTempFile("kanban_several", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = new Task("Задача", "Описание");
        manager.addTask(task);

        Epic epic = new Epic("Эпик", "Описание эпика");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic.getId());
        subtask.setStatus(Status.DONE);
        manager.addSubtask(subtask);
        manager.updateSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        List<Task> loadedTasks = loaded.getAllTasks();
        List<Epic> loadedEpics = loaded.getAllEpics();
        List<Subtask> loadedSubtasks = loaded.getAllSubtasks();

        assertEquals(1, loadedTasks.size(), "Должна быть 1 задача");
        assertEquals(1, loadedEpics.size(), "Должен быть 1 эпик");
        assertEquals(1, loadedSubtasks.size(), "Должна быть 1 подзадача");

        Task loadedTask = loadedTasks.get(0);
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());

        Subtask loadedSubtask = loadedSubtasks.get(0);
        assertEquals(subtask.getId(), loadedSubtask.getId());
        assertEquals(Status.DONE, loadedSubtask.getStatus());
        assertEquals(epic.getId(), loadedSubtask.getEpicId());

        Epic loadedEpic = loadedEpics.get(0);
        assertEquals(1, loadedEpic.getSubtaskIds().size(), "У эпика должна восстановиться связь с подзадачей");
    }

    @Test
    void loadSeveralTasksRestoresIds() throws IOException {
        File file = File.createTempFile("kanban_ids", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.addTask(new Task("T1", "D1"));
        manager.addTask(new Task("T2", "D2"));

        Epic epic = new Epic("E1", "DE1");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("S1", "DS1", epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(manager.getAllTasks().size(), loaded.getAllTasks().size());
        assertEquals(manager.getAllEpics().size(), loaded.getAllEpics().size());
        assertEquals(manager.getAllSubtasks().size(), loaded.getAllSubtasks().size());

        for (Task original : manager.getAllTasks()) {
            Task restored = loaded.getTask(original.getId());
            assertNotNull(restored, "Задача с id=" + original.getId() + " не восстановлена");
            assertEquals(original.getName(), restored.getName());
        }
    }
}
