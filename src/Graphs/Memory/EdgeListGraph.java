package Graphs.Memory;

import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;

public class EdgeListGraph<T> implements IGraph<T> {
    private final Set<T> nodes;
    private final List<Edge<T>> edges;
    private static final Random rand = new Random(8675309);

    public EdgeListGraph() {
        this.nodes = new HashSet<>();
        this.edges = new ArrayList<>();
    }

    @Override
    public Iterable<T> getNodes() {
        return nodes;
    }

    @Override
    public void addNode(T node) {
        nodes.add(node);
    }

    @Override
    public void addRelationship(String label, T source, T target) {
        // Ensure both nodes exist.
        addNode(source);
        addNode(target);
        edges.add(new Edge<>(source, target, label));
    }

    @Override
    public Iterable<Edge<T>> getRelationships(T node) {
        var neighbors = new HashSet<Edge<T>>();
        for (var edge : edges) {
            if (edge.getSource().equals(node)) {
                neighbors.add(edge);
            }
        }
        return neighbors;
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) {
            return null;
        }
        int index = rand.nextInt(nodes.size());
        return new ArrayList<>(nodes).get(index);
    }

    @Override
    public Edge<T> getRandomRelationship(T node) {
        var candidateEdges = edges.stream().filter(edge -> edge.getSource() == node).toList();
        if (candidateEdges.isEmpty()) {
            return null;
        }
        return candidateEdges.get(rand.nextInt(candidateEdges.size()));
    }

    @Override
    public void clear() {
        nodes.clear();
        edges.clear();
    }

    @Override
    public void close() {}
}
