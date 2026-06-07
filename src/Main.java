import manager.Managers;
import manager.TaskManager;
import task.Task;
import task.Epic;
import task.Subtask;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Две задачи
        Task task1 = new Task("Task1", "Описание первой задачи");
        Task task2 = new Task("Task2", "Описание второй задачи");
        manager.addTask(task1);
        manager.addTask(task2);

        // Эпик с тремя подзадачами
        Epic epicWithSubtasks = new Epic("Эпик с подзадачами", "Эпик с тремя подзадачами");
        Epic emptyEpic = new Epic("Пустой эпик", "Эпик без подзадач");
        manager.addEpic(epicWithSubtasks);
        manager.addEpic(emptyEpic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epicWithSubtasks.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epicWithSubtasks.getId());
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", epicWithSubtasks.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);

        printViewedTask("Запросили задачу 1", manager.getTask(task1.getId()), manager);
        printViewedTask("Запросили эпик с подзадачами", manager.getEpic(epicWithSubtasks.getId()), manager);
        printViewedTask("Запросили подзадачу 2", manager.getSubtask(subtask2.getId()), manager);
        printViewedTask("Повторно запросили задачу 1", manager.getTask(task1.getId()), manager);
        printViewedTask("Запросили пустой эпик", manager.getEpic(emptyEpic.getId()), manager);
        printViewedTask("Повторно запросили подзадачу 2", manager.getSubtask(subtask2.getId()), manager);

        // Удаление
        manager.deleteTask(task1.getId());
        printHistory("После удаления задачи 1", manager);

        manager.deleteEpic(epicWithSubtasks.getId());
        printHistory("После удаления эпика с подзадачами", manager);
    }

    private static void printViewedTask(String action, Task task, TaskManager manager) {
        System.out.println(action + ": " + task);
        printHistory("История просмотров", manager);
    }

    private static void printHistory(String title, TaskManager manager) {
        System.out.println(title + ":");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();
    }
}
