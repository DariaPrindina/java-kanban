import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import task.Task;
import task.Epic;
import task.Subtask;
import task.Status;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager manager = Managers.getDefault();

        // Две задачи
        Task task1 = new Task("Task1", "Описание первой задачи");
        Task task2 = new Task("Task2", "Описание второй задачи");
        manager.addTask(task1);
        manager.addTask(task2);

        // Эпик с двумя подзадачами
        Epic epic1 = new Epic("Эпик1", "Описание первого эпика");
        manager.addEpic(epic1);
        Subtask sub1 = new Subtask("Подзадача1 эпика1", "Описание подзадачи1 эпика1", epic1.getId());
        Subtask sub2 = new Subtask("Подзадача2 эпика1", "Описание подзадачи2 эпика1", epic1.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        // Эпик с одной подзадачей
        Epic epic2 = new Epic("Эпик2", "Описание второго эпика");
        manager.addEpic(epic2);
        Subtask sub3 = new Subtask("Подзадача1 эпика2", "Описание подзадачи1 эпика2", epic2.getId());
        manager.addSubtask(sub3);

        // Печать списков
        System.out.println("\n СПИСКИ");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        // Изменение статусов
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);
        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        sub3.setStatus(Status.DONE);
        manager.updateSubtask(sub3);

        System.out.println("\n ПОСЛЕ ИЗМЕНЕНИЯ СТАТУСОВ");
        System.out.println("Задача1: " + manager.getTask(task1.getId()));
        System.out.println("Эпик1: " + manager.getEpic(epic1.getId()));
        System.out.println("Эпик2: " + manager.getEpic(epic2.getId()));

        // Удаление
        manager.deleteTask(task2.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\n ПОСЛЕ УДАЛЕНИЯ");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        manager.getTask(task1.getId());
        manager.getEpic(epic2.getId());
        manager.getSubtask(sub3.getId());
        
        System.out.println("\nИстория просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
