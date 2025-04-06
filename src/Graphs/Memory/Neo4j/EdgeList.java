package Graphs.Memory.Neo4j;

import Graphs.Edge;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

public class EdgeList<T> implements Iterable<Edge<T>> {
    private EdgeNode<T> head;
    private EdgeNode<T> tail;

    public EdgeList() {
        head = null;
        tail = null;
    }

    /**
     * Appends an edge to the end of the linked list.
     */
    public void add(Edge<T> edge) {
        EdgeNode<T> newNode = new EdgeNode<>(edge);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
    }

    /**
     * Removes the first edge in the list that has the given target (and matching label).
     * Returns true if an edge was removed.
     */
    public boolean remove(String label, T target) {
        EdgeNode<T> current = head;
        EdgeNode<T> prev = null;
        while (current != null) {
            if (current.edge.getLabel().equals(label) && current.edge.getTarget().equals(target)) {
                if (prev == null) {
                    head = current.next;
                    if (head == null) {
                        tail = null;
                    }
                } else {
                    prev.next = current.next;
                    if (current.next == null) {
                        tail = prev;
                    }
                }
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    /**
     * Removes all edges in the list whose target equals the given target.
     */
    public void removeIfTargetEquals(T target) {
        EdgeNode<T> current = head;
        EdgeNode<T> prev = null;
        while (current != null) {
            if (current.edge.getTarget().equals(target)) {
                if (prev == null) {
                    head = current.next;
                    if (head == null) {
                        tail = null;
                    }
                    current = head;
                } else {
                    prev.next = current.next;
                    if (current.next == null) {
                        tail = prev;
                    }
                    current = prev.next;
                }
            } else {
                prev = current;
                current = current.next;
            }
        }
    }

    /**
     * Returns a random edge from this linked list using reservoir sampling.
     */
    public Edge<T> getRandomEdge(Random rand) {
        int count = 0;
        Edge<T> chosenEdge = null;
        for (Edge<T> edge : this) {
            count++;
            if (rand.nextInt(count) == 0) {
                chosenEdge = edge;
            }
        }
        return chosenEdge;
    }

    /**
     * Checks whether the list is empty.
     */
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public Iterator<Edge<T>> iterator() {
        return new Iterator<>() {
            private EdgeNode<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Edge<T> next() {
                if (current == null) throw new NoSuchElementException();
                Edge<T> edge = current.edge;
                current = current.next;
                return edge;
            }
        };
    }
}
