package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void subtaskCannotHaveItselfAsEpic() {
        Subtask subtask = new Subtask("Subtask", "Desc", 1);
        subtask.setId(2);

        assertNotEquals(subtask.getEpicId(), subtask.getId());
    }
}