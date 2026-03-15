package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> historyIndex = new HashMap<>();
    private Node head;
    private Node tail;

    private Node linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }

        return newNode;
    }

    private void removeNode(Node node) {
        Node prevNode = node.prev;
        Node nextNode = node.next;

        if (prevNode == null) {
            head = nextNode;
        } else {
            prevNode.next = nextNode;
        }

        if (nextNode == null) {
            tail = prevNode;
        } else {
            nextNode.prev = prevNode;
        }
    }

    private Task copyTask(Task task) {
        if (task instanceof Subtask subtask) {
            return new Subtask(subtask);
        }
        if (task instanceof Epic epic) {
            return new Epic(epic);
        }
        return new Task(task);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        Node newTail = linkLast(copyTask(task));
        historyIndex.put(task.getId(), newTail);
    }

    @Override
    public void remove(int id) {
        Node node = historyIndex.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(copyTask(current.task));
            current = current.next;
        }
        return tasks;
    }

    private static class Node {
        private final Task task;
        private Node prev;
        private Node next;

        private Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }
}
