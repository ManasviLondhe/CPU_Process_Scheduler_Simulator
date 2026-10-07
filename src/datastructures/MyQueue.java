package datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom FIFO queue built on linked nodes (head = front, tail = rear).
 * Used by FCFS and by FCFS levels of the multilevel schedulers.
 *
 * <pre>
 * enqueue  O(1)    dequeue O(1)    peek O(1)
 * isEmpty  O(1)    size    O(1)    clear O(1)
 * </pre>
 * Space: O(n).
 */
public class MyQueue<T> implements QueueADT<T> {
    private QueueNode<T> head;
    private QueueNode<T> tail;
    private int size;

    @Override
    public void enqueue(T item) {
        QueueNode<T> node = new QueueNode<>(item);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    @Override
    public T dequeue() {
        if (head == null) {
            return null;
        }
        T value = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return value;
    }

    @Override
    public T peek() {
        return head == null ? null : head.data;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        head = tail = null;
        size = 0;
    }

    @Override
    public List<T> toList() {
        List<T> result = new ArrayList<>(size);
        for (QueueNode<T> cur = head; cur != null; cur = cur.next) {
            result.add(cur.data);
        }
        return result;
    }

    @Override
    public String structureName() {
        return "Queue (linked FIFO)";
    }
}
