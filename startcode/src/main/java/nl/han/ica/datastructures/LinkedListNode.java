package nl.han.ica.datastructures;

public class LinkedListNode<T> {
    T value;
    LinkedListNode<T> next;

    public LinkedListNode(T value, LinkedListNode<T> next) {
        this.value = value;
        this.next = next;
    }

    public LinkedListNode() {
        value = null;
        next = null;
    }
}
