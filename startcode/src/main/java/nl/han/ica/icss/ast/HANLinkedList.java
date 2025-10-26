package nl.han.ica.icss.ast;

import nl.han.ica.datastructures.IHANLinkedList;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private Node<T> head;
    private int size;

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    @Override
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.next = head;
        head = newNode;
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
            addFirst(value);
            return;
        }

        Node<T> prev = getNode(index - 1);
        Node<T> newNode = new Node<>(value);
        newNode.next = prev.next;
        prev.next = newNode;
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

        Node<T> prev = getNode(pos - 1);
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
        if (head == null)
            throw new NoSuchElementException();

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

    private Node<T> getNode(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++)
            current = current.next;
        return current;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

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

