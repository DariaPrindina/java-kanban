package manager;

import task.Epic;
import task.Subtask;
import task.Status;
import task.Task;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
    );

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateId() {
        return nextId++;
    }

    private void syncNextId(int id) {
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    private boolean hasTimeOverlap(Task a, Task b) {
        if (a.getStartTime() == null || b.getStartTime() == null
                || a.getEndTime() == null || b.getEndTime() == null) {
            return false;
        }
        return a.getStartTime().isBefore(b.getEndTime())
                && b.getStartTime().isBefore(a.getEndTime());
    }

    private boolean isOverlapping(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }
        return getPrioritizedTasks().stream()
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> hasTimeOverlap(task, t));
    }

    private void updateEpicStatus(Epic epic) {
        List<Subtask> epicSubtasks = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = epicSubtasks.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(s -> s.getStatus() == Status.DONE);

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpicTimeFields(Epic epic) {
        List<Subtask> epicSubtasks = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        epic.setStartTime(epicSubtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null));

        epic.setEndTime(epicSubtasks.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null));

        Optional<Duration> totalDuration = epicSubtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration::plus);
        epic.setDuration(totalDuration.orElse(null));
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            removeFromPrioritized(task);
        });
        tasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача с id=" + id + " не найдена");
        }
        historyManager.add(task);
        return copyTask(task);
    }

    @Override
    public void addTask(Task task) {
        if (isOverlapping(task)) {
            throw new IllegalArgumentException(
                    "Задача пересекается по времени с существующей: " + task.getName());
        }
        if (task.getId() == 0) {
            task.setId(generateId());
        } else {
            syncNextId(task.getId());
        }
        Task copy = copyTask(task);
        tasks.put(copy.getId(), copy);
        addToPrioritized(copy);
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return;
        }
        if (isOverlapping(task)) {
            throw new IllegalArgumentException(
                    "Задача пересекается по времени с существующей: " + task.getName());
        }
        removeFromPrioritized(tasks.get(task.getId()));
        Task copy = copyTask(task);
        tasks.put(copy.getId(), copy);
        addToPrioritized(copy);
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritized(task);
        }
        historyManager.remove(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.values().stream()
                .flatMap(epic -> epic.getSubtaskIds().stream())
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .forEach(subtask -> {
                    removeFromPrioritized(subtask);
                    historyManager.remove(subtask.getId());
                });
        subtasks.clear();
        epics.values().forEach(epic -> historyManager.remove(epic.getId()));
        epics.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + id + " не найден");
        }
        historyManager.add(epic);
        return copyEpic(epic);
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic.getId() == 0) {
            epic.setId(generateId());
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

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        historyManager.remove(id);
        epic.getSubtaskIds().forEach(subId -> {
            Subtask removed = subtasks.remove(subId);
            if (removed != null) {
                removeFromPrioritized(removed);
            }
            historyManager.remove(subId);
        });
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + epicId + " не найден");
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(this::copySubtask)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            removeFromPrioritized(subtask);
        });
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            updateEpicStatus(epic);
            updateEpicTimeFields(epic);
        });
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с id=" + id + " не найдена");
        }
        historyManager.add(subtask);
        return copySubtask(subtask);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (isOverlapping(subtask)) {
            throw new IllegalArgumentException(
                    "Подзадача пересекается по времени с существующей: " + subtask.getName());
        }
        if (subtask.getId() == 0) {
            subtask.setId(generateId());
        } else {
            syncNextId(subtask.getId());
        }
        Subtask copy = copySubtask(subtask);
        subtasks.put(copy.getId(), copy);
        addToPrioritized(copy);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(copy.getId());
            updateEpicStatus(epic);
            updateEpicTimeFields(epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return;
        }
        if (isOverlapping(subtask)) {
            throw new IllegalArgumentException(
                    "Подзадача пересекается по времени с существующей: " + subtask.getName());
        }
        removeFromPrioritized(subtasks.get(subtask.getId()));
        Subtask copy = copySubtask(subtask);
        subtasks.put(copy.getId(), copy);
        addToPrioritized(copy);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            updateEpicTimeFields(epic);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }
        historyManager.remove(id);
        removeFromPrioritized(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
            updateEpicTimeFields(epic);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
