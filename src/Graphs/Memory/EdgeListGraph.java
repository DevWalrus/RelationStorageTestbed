package Graphs.Memory;

import Exceptions.InvalidNodeAccessException;
import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;

public class EdgeListGraph<T> implements IGraph<T> {
    private final List<T> nodes = new ArrayList<>();
    private final List<Edge<T>> edges = new ArrayList<>();
    private static final Random rand = new Random(8675309);

    @Override
    public Iterator<T> getNodes() {
        return nodes.iterator();
    }

    @Override
    public void addNode(T node) {
        nodes.add(node);
    }

    @Override
    public void addRelationship(String label, T source, T target) throws InvalidNodeAccessException {
        if (!nodes.contains(source)) {
            throw new InvalidNodeAccessException("The source node is not in the graph.");
        }
        if (!nodes.contains(target)) {
            throw new InvalidNodeAccessException("The target node is not in the graph.");
        }

        addNode(source);
        addNode(target);
        edges.add(new Edge<>(source, target, label));
    }

    @Override
    public Iterator<Edge<T>> getRelationships(T node) {
        return edges
                .stream()
                .filter(edge -> edge.getSource() == node)
                .iterator();
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
        var candidateEdges = new ArrayList<Edge<T>>();
        getRelationships(node).forEachRemaining(candidateEdges::add);
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
