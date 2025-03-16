package Graphs;

import java.util.*;

public class EdgeListGraph<T> implements IGraph<T> {
    // Set of nodes.
    private final Set<T> nodes;
    // List of edges.
    private final List<Edge<T>> edges;
    // Random instance for random selections.
    private final Random rand;

    public EdgeListGraph() {
        this.nodes = new HashSet<>();
        this.edges = new ArrayList<>();
        this.rand = new Random();
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
    public void removeNode(T node) {
        nodes.remove(node);
        // Remove any edge that touches the node.
        edges.removeIf(edge -> edge.getSource().equals(node) || edge.getTarget().equals(node));
    }

    @Override
    public void addEdge(String type, T source, T target) {
        // Ensure both nodes exist.
        addNode(source);
        addNode(target);
        edges.add(new Edge<>(source, target, type));
    }

    @Override
    public void removeEdge(String type, T source, T target) {
        edges.removeIf(edge -> edge.getLabel().equals(type) &&
                edge.getSource().equals(source) &&
                edge.getTarget().equals(target));
    }

    @Override
    public Iterable<T> getNeighbors(T node) {
        Set<T> neighbors = new HashSet<>();
        for (Edge<T> edge : edges) {
            // Treat graph as undirected: check both source and target.
            if (edge.getSource().equals(node)) {
                neighbors.add(edge.getTarget());
            }
            if (edge.getTarget().equals(node)) {
                neighbors.add(edge.getSource());
            }
        }
        return neighbors;
    }

    @Override
    public Iterable<T> getNeighbors(T node, String type) {
        Set<T> neighbors = new HashSet<>();
        for (Edge<T> edge : edges) {
            if (edge.getLabel().equals(type)) {
                if (edge.getSource().equals(node)) {
                    neighbors.add(edge.getTarget());
                }
                if (edge.getTarget().equals(node)) {
                    neighbors.add(edge.getSource());
                }
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
    public Edge<T> getRandomEdge(T node) {
        List<Edge<T>> candidateEdges = new ArrayList<>();
        for (Edge<T> edge : edges) {
            if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
                candidateEdges.add(edge);
            }
        }
        if (candidateEdges.isEmpty()) {
            return null;
        }
        return candidateEdges.get(rand.nextInt(candidateEdges.size()));
    }

    @Override
    public Edge<T> getRandomEdge(T node, String type) {
        List<Edge<T>> candidateEdges = new ArrayList<>();
        for (Edge<T> edge : edges) {
            if (edge.getLabel().equals(type) &&
                    (edge.getSource().equals(node) || edge.getTarget().equals(node))) {
                candidateEdges.add(edge);
            }
        }
        if (candidateEdges.isEmpty()) {
            return null;
        }
        return candidateEdges.get(rand.nextInt(candidateEdges.size()));
    }
}
