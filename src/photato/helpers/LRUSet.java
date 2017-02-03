package photato.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LRUSet<T> {

    private static class Node<T> {

        public final T value;
        public final long weight;
        public Node next;
        public Node previous;

        public Node(T value, long weight) {
            this.value = value;
            this.weight = weight;
        }

        public Node(T value, long weight, Node next, Node previous) {
            this.value = value;
            this.weight = weight;
            this.next = next;
            this.previous = previous;
        }

    }

    private final Node<T> firstNode;
    private final Node<T> lastNode;
    private final Map<T, Node<T>> map;
    private long totalWeight;

    public LRUSet() {
        this.map = new HashMap<>();
        this.totalWeight = 0;
        this.firstNode = new Node<>(null, 0, null, null);
        this.lastNode = new Node<>(null, 0, null, null);

        this.firstNode.next = this.lastNode;
        this.lastNode.previous = this.firstNode;
    }

    public synchronized void add(T t, long weight) {
        if (!this.map.containsKey(t)) {
            Node<T> n = new Node<>(t, weight);
            this.map.put(t, n);
            this.setFirst(n);
            this.totalWeight += weight;
        }
    }

    public synchronized void remove(T t) {
        Node<T> n = this.map.remove(t);
        if (n != null) {
            n.previous = n.next.previous;
            n.next = n.previous.next;
            this.totalWeight -= n.weight;
        }
    }

    public synchronized T removeLast() {
        if (this.size() > 0) {
            Node<T> last = this.lastNode.previous;
            this.lastNode.previous = last.previous;
            last.previous.next = this.lastNode;
            this.map.remove(last.value);
            this.totalWeight -= last.weight;

            return last.value;
        } else {
            return null;
        }
    }

    public synchronized void ping(T t) {
        Node<T> n = this.map.get(t);
        if (n != null) {
            this.setFirst(n);
        } else {
            throw new IllegalArgumentException();
        } 
    }

    public synchronized Collection<T> values() {
        return this.map.keySet();
    }

    public synchronized int size() {
        return this.map.size();
    }

    public synchronized long totalWeight() {
        return this.totalWeight;
    }

    private void setFirst(Node<T> n) {
        Node<T> nnext = n.next;
        Node<T> nprevious = n.previous;

        if (nnext != null) {
            nnext.previous = nprevious;
        }

        if (nprevious != null) {
            nprevious.next = nnext;
        }

        Node<T> firstNext = this.firstNode.next;
        this.firstNode.next = n;
        firstNext.previous = n;
        n.previous = this.firstNode;
        n.next = firstNext;

    }

}
