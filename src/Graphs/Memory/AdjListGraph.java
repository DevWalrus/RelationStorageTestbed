package Graphs.Memory;

import Exceptions.InvalidNodeAccessException;
import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;

public class AdjListGraph<T> implements IGraph<T> {
    private final List<T> nodes = new ArrayList<>();
    private final Map<T, List<Edge<T>>> adj = new HashMap<>();
    private static final Random rand = new Random(8675309);

    @Override
    public void addNode(T node) {
        nodes.add(node);
        adj.putIfAbsent(node, new ArrayList<>());
    }

    @Override
    public void addRelationship(String label, T source, T target) throws InvalidNodeAccessException {
        if (!nodes.contains(source)) {
            throw new InvalidNodeAccessException("The source node is not in the graph.");
        }
        if (!nodes.contains(target)) {
            throw new InvalidNodeAccessException("The target node is not in the graph.");
        }

        adj.get(source).add(new Edge<>(source, target, label));
    }

    @Override
    public Iterator<Edge<T>> getRelationships(T node) {
        return adj.get(node).iterator();
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) return null;
        int randomIndex = rand.nextInt(nodes.size());
        return nodes.get(randomIndex);
    }

    @Override
    public Edge<T> getRandomRelationship(T node) {
        var edges = adj.get(node);
        if (edges == null || edges.isEmpty()) return null;
        int randomIndex = rand.nextInt(edges.size());
        return edges.get(randomIndex);
    }

    @Override
    public Iterator<T> getNodes() {
        return nodes.iterator();
    }

    @Override
    public void clear() {
        nodes.clear();
        adj.clear();
    }

    @Override
    public void close() {}
}
