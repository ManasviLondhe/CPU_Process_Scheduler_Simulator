package datastructures;

import java.util.List;

/**
 * Common contract for the queue-like structures used by the schedulers.
 * MyQueue, MyCircularQueue and MyPriorityQueue all implement it, so a
 * multilevel scheduler can mix different structures per level.
 * <p>
 * dequeue() and peek() return null when the structure is empty.
 */
public interface QueueADT<T> {
    void enqueue(T item);
    T dequeue();
    T peek();
    boolean isEmpty();
    int size();
    void clear();

    /** Snapshot of the content in service order (next item to be served first). */
    List<T> toList();

    /** Human readable structure name shown by the UI. */
    String structureName();
}
