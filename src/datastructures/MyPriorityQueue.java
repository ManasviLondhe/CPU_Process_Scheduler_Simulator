package datastructures;

import java.util.Comparator;
import java.util.List;

/**
 * Priority queue = QueueADT view on top of the custom MinHeap.
 * "Smaller" according to the comparator is served first.
 * Used by SJF, SRTF, Priority scheduling and priority levels of MLQ.
 *
 * <pre>
 * enqueue O(log n)   dequeue O(log n)   peek O(1)   rebuild O(n)
 * </pre>
 */
public class MyPriorityQueue<T> implements QueueADT<T> {
    private final MinHeap<T> heap;

    public MyPriorityQueue(Comparator<? super T> comparator) {
        this.heap = new MinHeap<>(comparator);
    }

    @Override
    public void enqueue(T item) {
        heap.insert(item);
    }

    @Override
    public T dequeue() {
        return heap.extractMin();
    }

    @Override
    public T peek() {
        return heap.peek();
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public void clear() {
        heap.clear();
    }

    /** Content in service order (best first). O(n log n); used only for display and aging. */
    @Override
    public List<T> toList() {
        return heap.toSortedList();
    }

    /** Call after the sort keys of stored elements changed (aging). */
    public void rebuild() {
        heap.rebuild();
    }

    @Override
    public String structureName() {
        return "Priority Queue (binary min-heap)";
    }
}
