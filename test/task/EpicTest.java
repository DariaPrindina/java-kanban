package task;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void epicCannotBeAddedAsItsOwnSubtask() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);

        epic.addSubtaskId(1);

        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void epicStatusAllNew() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("S1", "D", epic.getId()));
        manager.addSubtask(new Subtask("S2", "D", epic.getId()));

        assertEquals(Status.NEW, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("S1", "D", epic.getId());
        Subtask s2 = new Subtask("S2", "D", epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);

        s1.setStatus(Status.DONE);
        s2.setStatus(Status.DONE);
        manager.updateSubtask(s1);
        manager.updateSubtask(s2);

        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusMixedNewAndDone() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("S1", "D", epic.getId());
        Subtask s2 = new Subtask("S2", "D", epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);

        s2.setStatus(Status.DONE);
        manager.updateSubtask(s2);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgress() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("S1", "D", epic.getId());
        manager.addSubtask(s1);

        s1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(s1);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicTimeFieldsCalculatedFromSubtasks() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("S1", "D", epic.getId());
        s1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        s1.setDuration(Duration.ofMinutes(30));

        Subtask s2 = new Subtask("S2", "D", epic.getId());
        s2.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        s2.setDuration(Duration.ofMinutes(60));

        manager.addSubtask(s1);
        manager.addSubtask(s2);

        Epic saved = manager.getEpic(epic.getId());

        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), saved.getStartTime(),
                "Начало эпика = начало самой ранней подзадачи");
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), saved.getEndTime(),
                "Конец эпика = конец самой поздней подзадачи");
        assertEquals(Duration.ofMinutes(90), saved.getDuration(),
                "Длительность эпика = сумма длительностей подзадач");
    }
}
