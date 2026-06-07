package manager;

import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import task.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,epic");
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
        TaskType type;
        if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicId());
        } else if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else {
            type = TaskType.TASK;
        }
        return task.getId() + "," + type + "," + task.getName() + "," +
                task.getStatus() + "," + task.getDescription() + "," + epicId;
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
            if (task instanceof Epic) {
                manager.addEpic((Epic) task);
            } else if (task instanceof Subtask) {
                subtasks.add((Subtask) task);
            } else {
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
        manager.addTask(task1);

        Task task2 = new Task("Задача 2", "Описание задачи 2");
        manager.addTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic1.getId());
        subtask2.setStatus(Status.DONE);
        manager.addSubtask(subtask2);
        manager.updateSubtask(subtask2);

        Epic epic2 = new Epic("Эпик 2 пустой", "Без подзадач");
        manager.addEpic(epic2);

        System.out.println("=== Исходный менеджер ===");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        System.out.println("\n=== Загруженный менеджер ===");
        System.out.println("Задачи: " + loaded.getAllTasks());
        System.out.println("Эпики: " + loaded.getAllEpics());
        System.out.println("Подзадачи: " + loaded.getAllSubtasks());

        boolean tasksMatch = manager.getAllTasks().size() == loaded.getAllTasks().size();
        boolean epicsMatch = manager.getAllEpics().size() == loaded.getAllEpics().size();
        boolean subtasksMatch = manager.getAllSubtasks().size() == loaded.getAllSubtasks().size();

        System.out.println("\nЗадачи совпадают: " + tasksMatch);
        System.out.println("Эпики совпадают: " + epicsMatch);
        System.out.println("Подзадачи совпадают: " + subtasksMatch);
    }
}
