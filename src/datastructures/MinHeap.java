package datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Array based binary min-heap ordered by a Comparator.
 * Parent of index i is (i-1)/2; children are 2i+1 and 2i+2.
 *
 * <pre>
 * insert       O(log n)
 * extractMin   O(log n)
 * peek         O(1)
 * heapify(all) O(n)      (bottom-up construction)
 * rebuild      O(n)      (restore heap property after keys changed, e.g. aging)
 * toSortedList O(n log n)
 * size/isEmpty O(1)
 * </pre>
 * Space: O(n).
 */
public class MinHeap<T> {
    private Object[] heap;
    private int size;
    private final Comparator<? super T> comparator;

    public MinHeap(Comparator<? super T> comparator) {
        this.comparator = comparator;
        this.heap = new Object[16];
    }

    public void insert(T item) {
        if (size == heap.length) {
            Object[] bigger = new Object[heap.length * 2];
            System.arraycopy(heap, 0, bigger, 0, size);
            heap = bigger;
        }
        heap[size] = item;
        siftUp(size);
        size++;
    }

    /** Removes and returns the smallest element, or null when empty. */
    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (size == 0) {
            return null;
        }
        T min = (T) heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return min;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        return size == 0 ? null : (T) heap[0];
    }

    /** Replaces the content with the given items and builds the heap bottom-up in O(n). */
    public void heapify(Collection<? extends T> items) {
        heap = new Object[Math.max(16, items.size() * 2)];
        size = 0;
        for (T item : items) {
            heap[size++] = item;
        }
        rebuild();
    }

    /** Re-establishes the heap property in O(n); call after element keys were modified. */
    public void rebuild() {
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        heap = new Object[16];
        size = 0;
    }

    /** Elements in ascending order; the heap itself is not modified. */
    @SuppressWarnings("unchecked")
    public List<T> toSortedList() {
        MinHeap<T> copy = new MinHeap<>(comparator);
        List<T> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add((T) heap[i]);
        }
        copy.heapify(items);
        List<T> sorted = new ArrayList<>(size);
        while (!copy.isEmpty()) {
            sorted.add(copy.extractMin());
        }
        return sorted;
    }

    @SuppressWarnings("unchecked")
    private int compareAt(int a, int b) {
        return comparator.compare((T) heap[a], (T) heap[b]);
    }

    private void swap(int a, int b) {
        Object tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    private void siftUp(int index) {
        int i = index;
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (compareAt(i, parent) >= 0) {
                break;
            }
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int index) {
        int i = index;
        while (true) {
            int left = 2 * i + 1;
            int right = left + 1;
            int smallest = i;
            if (left < size && compareAt(left, smallest) < 0) {
                smallest = left;
            }
            if (right < size && compareAt(right, smallest) < 0) {
                smallest = right;
            }
            if (smallest == i) {
                return;
            }
            swap(i, smallest);
            i = smallest;
        }
    }
}
