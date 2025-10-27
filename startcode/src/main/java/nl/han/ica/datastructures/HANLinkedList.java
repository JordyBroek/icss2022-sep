package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T>{

    private LinkedListNode<T> head;
    private int size;

    public HANLinkedList(){
        head = new LinkedListNode<>();
        size = 0;
    }

    @Override
    public void addFirst(T value){
        head = new LinkedListNode<>(value, head);
        size++;
    }


    @Override
    public void clear(){
        head = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value){
        LinkedListNode<T> prevNode = getNode(index - 1);
        if (prevNode != null) {
            prevNode.next = new LinkedListNode<>(value, prevNode.next);
            size++;
        }
    }

    @Override
    public void delete(int pos){
        LinkedListNode<T> prev = getNode(pos - 1);
        LinkedListNode<T> nodeToDelete = prev.next;
        prev.next = nodeToDelete.next;
        size--;
    }

    @Override
    public T get(int pos){
        LinkedListNode<T> node = getNode(pos);
        return node.value;
    }

    @Override
    public void removeFirst(){
        LinkedListNode<T> firstNode = getNode(0);
        head.next = firstNode.next;
        size--;
    }

    @Override
    public T getFirst(){
        LinkedListNode<T> firstNode = getNode(0);
        return firstNode.value;
    }

    @Override
    public int getSize(){
        return size;
    }

    private LinkedListNode<T> getNode(int pos){
        LinkedListNode<T> currentNode = head;
        for (int i = 0; i < pos; i++) {
            currentNode = currentNode.next;
        }
        return currentNode;
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i = 1; i< size - 1; i++){
            str.append(getNode(i).value).append(" ");
        }
        return str.toString();
    }
}

