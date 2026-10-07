package tests;

import static tests.TestUtil.assertEquals;
import static tests.TestUtil.check;

import java.util.Arrays;
import java.util.Comparator;

import datastructures.MinHeap;
import datastructures.MyCircularQueue;
import datastructures.MyLinkedList;
import datastructures.MyPriorityQueue;
import datastructures.MyQueue;

/** Tests of the custom data structures. */
public final class DataStructureTests {
    private DataStructureTests() {
    }

    public static void run() {
        TestUtil.section("MyLinkedList");
        MyLinkedList<Integer> list = new MyLinkedList<>();
        check("new list empty", list.isEmpty());
        list.add(10);
        list.add(20);
        list.add(30);
        list.addFirst(5);
        assertEquals("list order", Arrays.asList(5, 10, 20, 30), list.toList());
        assertEquals("get(2)", 20, list.get(2));
        assertEquals("indexOf(30)", 3, list.indexOf(30));
        assertEquals("indexOf missing", -1, list.indexOf(99));
        check("removeValue(10)", list.removeValue(10));
        assertEquals("removeAt(2) removes tail", 30, list.removeAt(2));
        list.add(40);
        assertEquals("tail pointer valid after removal", Arrays.asList(5, 20, 40), list.toList());
        assertEquals("size", 3, list.size());
        list.clear();
        check("cleared", list.isEmpty());

        TestUtil.section("MyQueue");
        MyQueue<String> q = new MyQueue<>();
        check("dequeue on empty is null", q.dequeue() == null);
        q.enqueue("a");
        q.enqueue("b");
        q.enqueue("c");
        assertEquals("peek", "a", q.peek());
        assertEquals("FIFO 1", "a", q.dequeue());
        assertEquals("FIFO 2", "b", q.dequeue());
        q.enqueue("d");
        assertEquals("toList", Arrays.asList("c", "d"), q.toList());
        assertEquals("size", 2, q.size());

        TestUtil.section("MyCircularQueue");
        MyCircularQueue<Integer> cq = new MyCircularQueue<>(3);
        cq.enqueue(1);
        cq.enqueue(2);
        cq.enqueue(3);
        check("full at capacity", cq.isFull());
        assertEquals("dequeue 1", 1, cq.dequeue());
        cq.enqueue(4);
        check("wrapped around and full again", cq.isFull());
        assertEquals("order after wrap", Arrays.asList(2, 3, 4), cq.toList());
        cq.enqueue(5);
        check("grew when full", cq.capacity() >= 6);
        assertEquals("order after growth", Arrays.asList(2, 3, 4, 5), cq.toList());
        cq.clear();
        check("empty after clear", cq.isEmpty() && cq.peek() == null);

        TestUtil.section("MinHeap / MyPriorityQueue");
        MinHeap<Integer> heap = new MinHeap<>(Comparator.naturalOrder());
        for (int v : new int[] {9, 4, 7, 1, 8, 2}) {
            heap.insert(v);
        }
        assertEquals("peek is minimum", 1, heap.peek());
        assertEquals("sorted view", Arrays.asList(1, 2, 4, 7, 8, 9), heap.toSortedList());
        assertEquals("extractMin 1", 1, heap.extractMin());
        assertEquals("extractMin 2", 2, heap.extractMin());
        heap.heapify(Arrays.asList(5, 3, 8, 1));
        assertEquals("heapify min", 1, heap.peek());
        assertEquals("heapify size", 4, heap.size());
        MyPriorityQueue<Integer> pq = new MyPriorityQueue<>(Comparator.reverseOrder());
        pq.enqueue(3);
        pq.enqueue(10);
        pq.enqueue(7);
        assertEquals("max-priority first", 10, pq.dequeue());
        assertEquals("next", 7, pq.dequeue());
    }
}
