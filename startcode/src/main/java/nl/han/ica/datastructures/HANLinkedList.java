package nl.han.ica.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private LinkedListNode<T> head;
    private int size;

    public HANLinkedList() {
        head = new LinkedListNode<>();
        size = 0;
    }

    @Override
    public void addFirst(T value) {
        head = new LinkedListNode<>(value, head);
        size++;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();
        if (index == 0) {
            head = new LinkedListNode<>(value, head);
            size++;
            return;
        }
        LinkedListNode<T> prevNode = getNode(index - 1);
        LinkedListNode<T> newNode = new LinkedListNode<>(value, prevNode.next);
        newNode.next = prevNode.next;
        prevNode.next = newNode;
        size++;
    }

    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= size)
            throw new IndexOutOfBoundsException();
        if (pos == 0) {
            removeFirst();
            return;
        }
        LinkedListNode<T> prev = getNode(pos - 1);
        prev.next = prev.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size)
            throw new IndexOutOfBoundsException();

        return getNode(pos).value;
    }

    @Override
    public void removeFirst() {
        if (head == null) {
            throw new NoSuchElementException();
        }
        head = head.next;
        size--;
    }

    @Override
    public T getFirst() {
        if (head == null)
            throw new NoSuchElementException();
        return head.value;
    }

    @Override
    public int getSize() {
        return size;
    }

    private LinkedListNode<T> getNode(int index) {
        LinkedListNode<T> current = head;
        for (int i = 0; i < index; i++)
            current = current.next;
        return current;
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i = 1; i< size - 1; i++){
            str.append(getNode(i).value).append(" ");
        }
        return str.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private LinkedListNode<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                T value = current.value;
                current = current.next;
                return value;
            }
        };
    }
}

