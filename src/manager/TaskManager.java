package manager;

import task.Task;
import task.Epic;
import task.Subtask;

import java.util.List;

public interface TaskManager {

    // Задачи
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTask(int id);

    void addTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);

    // Эпики
    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpic(int id);

    void addEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    // Подзадачи
    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtask(int id);

    void addSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    // Подзадачи конкретного эпика
    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}