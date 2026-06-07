package manager;

import task.Epic;
import task.Subtask;
import task.Status;
import task.Task;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateId() {
        return nextId++;
    }

    private void syncNextId(int id) {
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : new ArrayList<>(tasks.keySet())) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task == null ? null : copyTask(task);
    }

    @Override
    public void addTask(Task task) {
        if (task.getId() == 0) {
            int id = generateId();
            task.setId(id);
        } else {
            syncNextId(task.getId());
        }
        tasks.put(task.getId(), copyTask(task));
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), copyTask(task));
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : new ArrayList<>(epics.values())) {
            historyManager.remove(epic.getId());
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
        }
        epics.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic == null ? null : copyEpic(epic);
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic.getId() == 0) {
            int id = generateId();
            epic.setId(id);
        } else {
            syncNextId(epic.getId());
        }
        epics.put(epic.getId(), copyEpic(epic));
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), copyEpic(epic));
        }
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }

        historyManager.remove(id);
        for (int subId : epic.getSubtaskIds()) {
            subtasks.remove(subId);
            historyManager.remove(subId);
        }
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subId : subIds) {
            Subtask subtask = subtasks.get(subId);
            if (subtask == null) {
                continue;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return result;
        }
        for (int subId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subId);
            if (subtask != null) {
                result.add(copySubtask(subtask));
            }
        }
        return result;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(id);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask == null ? null : copySubtask(subtask);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask.getId() == 0) {
            int id = generateId();
            subtask.setId(id);
        } else {
            syncNextId(subtask.getId());
        }
        subtasks.put(subtask.getId(), copySubtask(subtask));

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return;
        }
        subtasks.put(subtask.getId(), copySubtask(subtask));

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }

        historyManager.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private Task copyTask(Task task) {
        return new Task(task);
    }

    private Epic copyEpic(Epic epic) {
        return new Epic(epic);
    }

    private Subtask copySubtask(Subtask subtask) {
        return new Subtask(subtask);
    }
}
