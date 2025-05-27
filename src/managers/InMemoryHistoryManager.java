package managers;

import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> nodeMap;
    private Node head;
    private Node tail;


    public InMemoryHistoryManager() {
        this.nodeMap = new HashMap<>();
        this.head = null;
        this.tail = null;
    }

    @Override
    public void add(Task task) {
        // Удаляем старый просмотр, если уже есть
        if (nodeMap.containsKey(task.getId())) {
            remove(task.getId());
        }
        // Добавляем в конец
        Node node = new Node(task);
        if (tail != null) {
            tail.next = node;
            node.prev = tail;
        } else {
            head = node;
        }
        tail = node;
        nodeMap.put(task.getId(), node);
    }

    @Override
    public void remove(int taskId) {
        Node node = nodeMap.remove(taskId);
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next; // удалён был первый
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev; // удалён был последний
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }
}
