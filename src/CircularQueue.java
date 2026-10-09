import java.util.NoSuchElementException;

public class CircularQueue<T> {

    private final Object[] elements;
    private int front;
    private int rear;
    private int size;

    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                "Queue capacity must be greater than zero."
            );
        }
        elements = new Object[capacity];
    }

    public void enqueue(T value) {
        if (value == null) {
            throw new IllegalArgumentException(
                "Queue cannot contain null values."
            );
        }
        if (isFull()) {
            throw new IllegalStateException("Queue is full.");
        }

        elements[rear] = value;
        rear = (rear + 1) % elements.length;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty.");
        }

        T value = (T) elements[front];
        elements[front] = null;
        front = (front + 1) % elements.length;
        size--;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty.");
        }
        return (T) elements[front];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == elements.length;
    }

    public int size() {
        return size;
    }
}