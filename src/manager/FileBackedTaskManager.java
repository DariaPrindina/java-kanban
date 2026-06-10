package manager;

import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import task.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,epic,startTime,duration");
        for (Task task : getAllTasks()) {
            lines.add(taskToString(task));
        }
        for (Epic epic : getAllEpics()) {
            lines.add(taskToString(epic));
        }
        for (Subtask subtask : getAllSubtasks()) {
            lines.add(taskToString(subtask));
        }
        try {
            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл: " + file.getName(), e);
        }
    }

    private String taskToString(Task task) {
        String epicId = "";
        if (task.getType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String duration = task.getDuration() != null
                ? String.valueOf(task.getDuration().toMinutes()) : "";

        return task.getId() + "," + task.getType() + "," + task.getName() + ","
                + task.getStatus() + "," + task.getDescription() + ","
                + epicId + "," + startTime + "," + duration;
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        Task task;
        switch (type) {
            case EPIC:
                task = new Epic(name, description);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                task = new Subtask(name, description, epicId);
                break;
            default:
                task = new Task(name, description);
        }
        task.setId(id);
        task.setStatus(status);

        if (fields.length > 6 && !fields[6].isBlank()) {
            task.setStartTime(LocalDateTime.parse(fields[6]));
        }
        if (fields.length > 7 && !fields[7].isBlank()) {
            task.setDuration(Duration.ofMinutes(Long.parseLong(fields[7])));
        }
        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        String content;
        try {
            content = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла: " + file.getName(), e);
        }
        if (content.isBlank()) {
            return manager;
        }
        String[] lines = content.split(System.lineSeparator());
        List<Subtask> subtasks = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) continue;
            Task task = fromString(lines[i]);
            switch (task.getType()) {
                case EPIC:
                    manager.addEpic((Epic) task);
                    break;
                case SUBTASK:
                    subtasks.add((Subtask) task);
                    break;
                default:
                    manager.addTask(task);
            }
        }
        for (Subtask subtask : subtasks) {
            manager.addSubtask(subtask);
        }
        return manager;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("kanban", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Задача 1", "Описание задачи 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.addTask(task1);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        subtask1.setDuration(Duration.ofMinutes(30));
        manager.addSubtask(subtask1);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        System.out.println("=== Загруженный менеджер ===");
        System.out.println("Задачи: " + loaded.getAllTasks());
        System.out.println("Эпики: " + loaded.getAllEpics());
        System.out.println("Подзадачи: " + loaded.getAllSubtasks());
        System.out.println("По приоритету: " + loaded.getPrioritizedTasks());
    }
}
