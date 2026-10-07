package datastructures;

/** A single node of a singly linked structure (used by MyLinkedList and MyQueue). */
public class QueueNode<T> {
    final T data;
    QueueNode<T> next;

    QueueNode(T data) {
        this.data = data;
    }
}
