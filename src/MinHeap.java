
import java.util.Comparator;

public class MinHeap {
    private Process[] heap;
    private int size;
    private final Comparator<Process> comparator;

    public MinHeap(int capacity, Comparator<Process> comparator) {
        if (capacity < 1) {
            capacity = 1;
        }

        heap = new Process[capacity];
        size = 0;
        this.comparator = comparator;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public Process peek() {
        if (isEmpty()) {
            return null;
        }
        return heap[0];
    }

    public void insert(Process process) {
        ensureCapacity();

        heap[size] = process;
        siftUp(size);
        size++;
    }

    public Process removeMin() {
        if (isEmpty()) {
            return null;
        }

        Process minimum = heap[0];
        size--;

        if (size > 0) {
            heap[0] = heap[size];
            siftDown(0);
        }

        heap[size] = null;
        return minimum;
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;

            if (comparator.compare(heap[index], heap[parent]) >= 0) {
                break;
            }

            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size &&
                comparator.compare(heap[left], heap[smallest]) < 0) {
                smallest = left;
            }

            if (right < size &&
                comparator.compare(heap[right], heap[smallest]) < 0) {
                smallest = right;
            }

            if (smallest == index) {
                break;
            }

            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        Process temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void ensureCapacity() {
        if (size == heap.length) {
            Process[] newHeap = new Process[heap.length * 2];
            System.arraycopy(heap, 0, newHeap, 0, size);
            heap = newHeap;
        }
    }
}
