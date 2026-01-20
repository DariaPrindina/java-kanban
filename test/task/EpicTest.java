package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void epicCannotBeAddedAsItsOwnSubtask() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);

        epic.addSubtaskId(1);

        assertTrue(epic.getSubtaskIds().isEmpty());
    }
}