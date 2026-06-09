package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("kanban_test", ".csv");
            tempFile.deleteOnExit();
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveAndLoadEmptyManager() throws IOException {
        File file = File.createTempFile("kanban_empty", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void saveAndLoadSeveralTasks() throws IOException {
        File file = File.createTempFile("kanban_several", ".csv");
        file.deleteOnExit();
        FileBackedTaskManager fm = new FileBackedTaskManager(file);

        Task task = new Task("Задача", "Описание");
        task.setStartTime(LocalDateTime.of(2024, 6, 1, 9, 0));
        task.setDuration(Duration.ofMinutes(60));
        fm.addTask(task);

        Epic epic = new Epic("Эпик", "Описание эпика");
        fm.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2024, 6, 1, 11, 0));
        subtask.setDuration(Duration.ofMinutes(30));
        subtask.setStatus(Status.DONE);
        fm.addSubtask(subtask);
        fm.updateSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubtasks().size());

        Task loadedTask = loaded.getTask(task.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());

        Subtask loadedSubtask = loaded.getAllSubtasks().get(0);
        assertEquals(Status.DONE, loadedSubtask.getStatus());
        assertEquals(epic.getId(), loadedSubtask.getEpicId());
        assertEquals(1, loaded.getEpic(epic.getId()).getSubtaskIds().size());
    }

    @Test
    void loadSeveralTasksRestoresIds() throws IOException {
        File file = File.createTempFile("kanban_ids", ".csv");
        file.deleteOnExit();
        FileBackedTaskManager fm = new FileBackedTaskManager(file);

        fm.addTask(new Task("T1", "D1"));
        fm.addTask(new Task("T2", "D2"));
        Epic epic = new Epic("E1", "DE1");
        fm.addEpic(epic);
        fm.addSubtask(new Subtask("S1", "DS1", epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(fm.getAllTasks().size(), loaded.getAllTasks().size());
        assertEquals(fm.getAllEpics().size(), loaded.getAllEpics().size());
        assertEquals(fm.getAllSubtasks().size(), loaded.getAllSubtasks().size());

        for (Task original : fm.getAllTasks()) {
            assertNotNull(loaded.getTask(original.getId()),
                    "Задача с id=" + original.getId() + " не восстановлена");
        }
    }

    @Test
    void loadFromNonExistentFileShouldThrowException() {
        File nonExistent = new File("/tmp/no_such_file_kanban_xyz.csv");
        assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(nonExistent));
    }

    @Test
    void saveToReadOnlyFileShouldThrowException() throws IOException {
        File file = File.createTempFile("kanban_readonly", ".csv");
        file.deleteOnExit();
        file.setWritable(false);

        FileBackedTaskManager fm = new FileBackedTaskManager(file);
        assertThrows(ManagerSaveException.class,
                () -> fm.addTask(new Task("T", "D")));
    }

    @Test
    void prioritizedTasksAreRestoredAfterLoad() throws IOException {
        File file = File.createTempFile("kanban_prio", ".csv");
        file.deleteOnExit();
        FileBackedTaskManager fm = new FileBackedTaskManager(file);

        Task task1 = new Task("Late", "Desc");
        task1.setStartTime(LocalDateTime.of(2024, 6, 1, 14, 0));
        task1.setDuration(Duration.ofMinutes(30));
        fm.addTask(task1);

        Task task2 = new Task("Early", "Desc");
        task2.setStartTime(LocalDateTime.of(2024, 6, 1, 9, 0));
        task2.setDuration(Duration.ofMinutes(30));
        fm.addTask(task2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> prioritized = loaded.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId(),
                "После загрузки приоритет должен быть сохранён");
    }
}
