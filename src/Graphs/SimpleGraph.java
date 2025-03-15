package Graphs;

import java.util.*;

public class SimpleGraph<T> implements IGraph<T> {
    private Set<T> nodes = new HashSet<>();
    private Map<T, Set<T>> adj = new HashMap<>();

    @Override
    public void addNode(T node) {
        nodes.add(node);
        if (!adj.containsKey(node)) {
            adj.put(node, new HashSet<>());
        }
    }

    @Override
    public void removeNode(T node) {
        nodes.remove(node);
        adj.remove(node);
        for (Set<T> neighbors : adj.values()) {
            neighbors.remove(node);
        }
    }

    @Override
    public void addEdge(String type, T source, T target) {
        // For an undirected graph, add both directions.
        addNode(source);
        addNode(target);
        adj.get(source).add(target);
        adj.get(target).add(source);
    }

    @Override
    public void removeEdge(String type, T source, T target) {
        if (adj.containsKey(source)) {
            adj.get(source).remove(target);
        }
        if (adj.containsKey(target)) {
            adj.get(target).remove(source);
        }
    }

    @Override
    public Iterable<T> getNeighbors(T node) {
        return adj.getOrDefault(node, new HashSet<>());
    }

    @Override
    public Iterable<T> getNeighbors(T node, String type) {
        return getNeighbors(node);
    }

    @Override
    public T getRandomNode() {
        List<T> list = new ArrayList<>(nodes);
        return list.get(new Random().nextInt(list.size()));
    }

    @Override
    public Edge<T> getRandomEdge(T node) {
        Iterator<T> it = getNeighbors(node).iterator();
        if (it.hasNext()) {
            return new Edge<>(node, it.next());
        }
        return null;
    }

    @Override
    public Edge<T> getRandomEdge(T node, String type) {
        return getRandomEdge(node);
    }

    // Additional method to expose all nodes.
    public Set<T> getNodes() {
        return nodes;
    }
}
