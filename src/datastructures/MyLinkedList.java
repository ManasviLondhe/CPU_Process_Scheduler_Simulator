package datastructures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom singly linked list with head and tail pointers.
 *
 * <pre>
 * Operation          Time
 * add (at tail)      O(1)
 * addFirst           O(1)
 * removeFirst        O(1)
 * get(index)         O(n)
 * removeAt(index)    O(n)
 * removeValue(x)     O(n)
 * indexOf / contains O(n)   (linear search)
 * size / isEmpty     O(1)
 * clear              O(1)
 * </pre>
 * Space: O(n).
 */
public class MyLinkedList<T> implements Iterable<T> {
    private QueueNode<T> head;
    private QueueNode<T> tail;
    private int size;

    public void add(T value) {
        QueueNode<T> node = new QueueNode<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void addFirst(T value) {
        QueueNode<T> node = new QueueNode<>(value);
        node.next = head;
        head = node;
        if (tail == null) {
            tail = node;
        }
        size++;
    }

    public T get(int index) {
        checkIndex(index);
        QueueNode<T> cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
        }
        return cur.data;
    }

    public T removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        T value = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return value;
    }

    public T removeAt(int index) {
        checkIndex(index);
        if (index == 0) {
            return removeFirst();
        }
        QueueNode<T> prev = head;
        for (int i = 0; i < index - 1; i++) {
            prev = prev.next;
        }
        QueueNode<T> target = prev.next;
        prev.next = target.next;
        if (target == tail) {
            tail = prev;
        }
        size--;
        return target.data;
    }

    /** Removes the first element equal to value. Returns true when something was removed. */
    public boolean removeValue(T value) {
        int index = indexOf(value);
        if (index < 0) {
            return false;
        }
        removeAt(index);
        return true;
    }

    /** Linear search. Returns the index of the first match or -1. */
    public int indexOf(T value) {
        int index = 0;
        for (QueueNode<T> cur = head; cur != null; cur = cur.next, index++) {
            if (cur.data == null ? value == null : cur.data.equals(value)) {
                return index;
            }
        }
        return -1;
    }

    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = tail = null;
        size = 0;
    }

    public List<T> toList() {
        List<T> result = new ArrayList<>(size);
        for (QueueNode<T> cur = head; cur != null; cur = cur.next) {
            result.add(cur.data);
        }
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private QueueNode<T> cur = head;

            @Override
            public boolean hasNext() {
                return cur != null;
            }

            @Override
            public T next() {
                T value = cur.data;
                cur = cur.next;
                return value;
            }
        };
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }
    }
}
