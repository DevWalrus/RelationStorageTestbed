package Graphs.Memory;

import Exceptions.InvalidNodeAccessException;
import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;

public class SimpleGraph<T> implements IGraph<T> {
    private final Set<T> nodes = new HashSet<>();
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
        adj.get(target).add(new Edge<>(target, source, label));
    }

    @Override
    public Iterable<Edge<T>> getRelationships(T node) {
        return adj.get(node);
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) return null;
        int randomIndex = rand.nextInt(nodes.size());
        return new ArrayList<>(nodes).get(randomIndex);
    }

    @Override
    public Edge<T> getRandomRelationship(T node) {
        var edges = adj.get(node);
        if (edges == null || edges.isEmpty()) return null;
        int count = 0;
        Edge<T> chosenEdge = null;
        for (var edge : edges) {
            count++;
            if (rand.nextInt(count) == 0) {
                chosenEdge = edge;
            }
        }
        return chosenEdge;
    }

    @Override
    public Iterable<T> getNodes() {
        return nodes;
    }

    @Override
    public void clear() {
        nodes.clear();
        adj.clear();
    }

    @Override
    public void close() {}
}
