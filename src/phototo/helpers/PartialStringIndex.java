package phototo.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PartialStringIndex<T> {

    private static class Node<T> {

        public final char c;
        public final Set<T> values;
        public int weight;
        public Node<T> firstChild;
        public Node<T> neighbor;

        public Node() {
            this('*');
        }

        public Node(char c) {
            this.c = c;
            this.values = new HashSet<>();
            this.weight = 1;
        }

        public Node<T> findChild(char c) {
            if (this.firstChild == null) {
                return null;
            } else {
                Node<T> n = this.firstChild;
                while (n != null && n.c != c) {
                    n = n.neighbor;
                }

                return n;
            }
        }

        public void addChild(char c, Node<T> newChild) {
            if (this.firstChild == null) {
                this.firstChild = newChild;
            } else {
                Node<T> lastChild = this.firstChild;
                while (lastChild.neighbor != null) {
                    lastChild = lastChild.neighbor;
                }

                lastChild.neighbor = newChild;
            }
        }

        public void removeChild(char c) {
            Node<T> previous = null;
            Node<T> current = this.firstChild;
            while (current != null) {
                if (current.c == c) {
                    if (current == this.firstChild) {
                        this.firstChild = current.neighbor;
                    } else {
                        previous.neighbor = current.neighbor;
                    }
                    break;
                } else {
                    previous = current;
                    current = current.neighbor;
                }
            }
        }

    }

    private final ReentrantReadWriteLock lock;
    private final Map<T, Set<String>> valuesMap;
    private final boolean prefixOnlyMode;
    private Node<T> rootNode;

    public PartialStringIndex() {
        this(false);
    }

    public PartialStringIndex(boolean prefixOnlyMode) {
        this.rootNode = new Node<>();
        this.valuesMap = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.prefixOnlyMode = prefixOnlyMode;
    }

    public void add(String indexKey, T value) {
        if (indexKey == null || indexKey.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }

        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }

        this.lock.writeLock().lock();
        try {
            this.rootNode.values.add(value);

            if (!this.valuesMap.containsKey(value)) {
                this.valuesMap.put(value, new HashSet<String>());
            }
            this.valuesMap.get(value).add(indexKey);

            for (int i = 0; i < (this.prefixOnlyMode ? 1 : indexKey.length()); i++) {
                Node<T> currentNode = this.rootNode;

                for (int j = i; j < indexKey.length(); j++) {
                    char c = indexKey.charAt(j);

                    Node<T> child = currentNode.findChild(c);
                    if (child == null) {
                        Node<T> newNode = new Node<>(c);
                        currentNode.addChild(c, newNode);
                        currentNode = newNode;
                    } else {
                        child.weight++;
                        currentNode = child;
                    }
                }

                currentNode.values.add(value);
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public Collection<T> findContains(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }

        this.lock.readLock().lock();
        try {
            Node<T> currentNode = this.rootNode;

            for (int i = 0; i < key.length(); i++) {
                char c = key.charAt(i);
                Node node = currentNode.findChild(c);

                if (node == null) {
                    return new HashSet<>();
                } else {
                    currentNode = node;
                }
            }

            // From now on, exploration of all nodes to add them to the result
            Set<T> result = new HashSet<>();

            Queue<Node<T>> toExplore = new LinkedList<>();
            toExplore.add(currentNode);

            while (!toExplore.isEmpty()) {
                Node<T> n = toExplore.remove();
                result.addAll(n.values);

                Node child = n.firstChild;
                while (child != null) {
                    toExplore.add(child);
                    child = child.neighbor;
                }
            }

            return result;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void remove(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }

        this.lock.writeLock().lock();
        try {
            if (this.valuesMap.containsKey(value)) {
                for (String key : this.valuesMap.get(value)) {
                    this.rootNode.values.remove(value);

                    for (int i = 0; i < (this.prefixOnlyMode ? 1 : key.length()); i++) {
                        Node<T> currentNode = this.rootNode;

                        for (int j = i; j < key.length(); j++) {
                            char c = key.charAt(j);

                            Node<T> n = currentNode.findChild(c);
                            if (n != null) {
                                n.weight--;

                                if (n.weight == 0) {
                                    currentNode.removeChild(c);
                                    break;
                                } else {
                                    currentNode = n;
                                }
                            } else {
                                break;
                            }
                        }

                        currentNode.values.remove(value);
                    }
                }

                this.valuesMap.remove(value);
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public Collection<T> values() {
        this.lock.readLock().lock();
        try {
            return this.rootNode.values;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public int size() {
        this.lock.readLock().lock();
        try {
            return this.rootNode.values.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void clear() {
        this.lock.writeLock().lock();
        try {
            this.rootNode = new Node<>();
            this.valuesMap.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

}
