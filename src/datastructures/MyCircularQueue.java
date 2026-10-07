package datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom circular queue stored in an array with wrap-around indices.
 * Used by Round Robin: the process that finishes its quantum goes to the rear
 * and the front moves on, so the queue "rotates".
 * <p>
 * To keep the scheduler simple the array doubles when it is full
 * (isFull() reports the state before the growth).
 *
 * <pre>
 * enqueue  O(1) amortised (O(n) only when the array grows)
 * dequeue  O(1)    peek O(1)    isEmpty O(1)    isFull O(1)    size O(1)
 * </pre>
 * Space: O(capacity).
 */
public class MyCircularQueue<T> implements QueueADT<T> {
    private Object[] data;
    private int front;
    private int count;

    public MyCircularQueue(int initialCapacity) {
        data = new Object[Math.max(1, initialCapacity)];
    }

    public MyCircularQueue() {
        this(8);
    }

    @Override
    public void enqueue(T item) {
        if (isFull()) {
            grow();
        }
        data[(front + count) % data.length] = item;
        count++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (count == 0) {
            return null;
        }
        T value = (T) data[front];
        data[front] = null;
        front = (front + 1) % data.length;
        count--;
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T peek() {
        return count == 0 ? null : (T) data[front];
    }

    public boolean isFull() {
        return count == data.length;
    }

    public int capacity() {
        return data.length;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public void clear() {
        for (int i = 0; i < data.length; i++) {
            data[i] = null;
        }
        front = 0;
        count = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> toList() {
        List<T> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add((T) data[(front + i) % data.length]);
        }
        return result;
    }

    @Override
    public String structureName() {
        return "Circular Queue (array, wrap-around)";
    }

    /** Doubles the array and unrolls the circular content so that front becomes index 0. */
    private void grow() {
        Object[] bigger = new Object[data.length * 2];
        for (int i = 0; i < count; i++) {
            bigger[i] = data[(front + i) % data.length];
        }
        data = bigger;
        front = 0;
    }
}
