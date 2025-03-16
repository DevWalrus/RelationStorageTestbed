package Graphs;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleGraph<T> implements IGraph<T> {
    // Set of nodes.
    private final Set<T> nodes = new HashSet<>();
    // Map from each node to a list of its outgoing Edge objects.
    private final Map<T, List<Edge<T>>> adj = new HashMap<>();

    @Override
    public void addNode(T node) {
        nodes.add(node);
        // Create an empty list for new node.
        adj.putIfAbsent(node, new ArrayList<>());
    }

    @Override
    public void removeNode(T node) {
        nodes.remove(node);
        adj.remove(node);
        // Remove any edge in other node's list that points to the removed node.
        for (List<Edge<T>> edges : adj.values()) {
            edges.removeIf(edge -> edge.getTarget().equals(node));
        }
    }

    /**
     * Adds an undirected edge between source and target.
     * This implementation adds the edge in both directions.
     */
    @Override
    public void addEdge(String type, T source, T target) {
        addNode(source);
        addNode(target);
        // Add edge from source to target.
        adj.get(source).add(new Edge<>(source, target, type));
        // For undirected graph, also add the reverse edge.
        adj.get(target).add(new Edge<>(target, source, type));
    }

    @Override
    public void removeEdge(String type, T source, T target) {
        List<Edge<T>> sourceEdges = adj.get(source);
        if (sourceEdges != null) {
            sourceEdges.removeIf(edge -> edge.getLabel().equals(type) && edge.getTarget().equals(target));
        }
        List<Edge<T>> targetEdges = adj.get(target);
        if (targetEdges != null) {
            targetEdges.removeIf(edge -> edge.getLabel().equals(type) && edge.getTarget().equals(source));
        }
    }

    @Override
    public Iterable<T> getNeighbors(T node) {
        List<T> result = new ArrayList<>();
        List<Edge<T>> edges = adj.get(node);
        if (edges != null) {
            for (Edge<T> edge : edges) {
                result.add(edge.getTarget());
            }
        }
        return result;
    }

    @Override
    public Iterable<T> getNeighbors(T node, String type) {
        List<T> result = new ArrayList<>();
        List<Edge<T>> edges = adj.get(node);
        if (edges != null) {
            for (Edge<T> edge : edges) {
                if (edge.getLabel().equals(type)) {
                    result.add(edge.getTarget());
                }
            }
        }
        return result;
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) return null;
        int randomIndex = ThreadLocalRandom.current().nextInt(nodes.size());
        return new ArrayList<>(nodes).get(randomIndex);
    }

    @Override
    public Edge<T> getRandomEdge(T node) {
        List<Edge<T>> edges = adj.get(node);
        if (edges == null || edges.isEmpty()) return null;
        Random rand = ThreadLocalRandom.current();
        int count = 0;
        Edge<T> chosenEdge = null;
        // Reservoir sampling over all edges from node.
        for (Edge<T> edge : edges) {
            count++;
            if (rand.nextInt(count) == 0) {
                chosenEdge = edge;
            }
        }
        return chosenEdge;
    }

    @Override
    public Edge<T> getRandomEdge(T node, String type) {
        List<Edge<T>> edges = adj.get(node);
        if (edges == null || edges.isEmpty()) return null;
        Random rand = ThreadLocalRandom.current();
        int count = 0;
        Edge<T> chosenEdge = null;
        for (Edge<T> edge : edges) {
            if (edge.getLabel().equals(type)) {
                count++;
                if (rand.nextInt(count) == 0) {
                    chosenEdge = edge;
                }
            }
        }
        return chosenEdge;
    }

    @Override
    public Iterable<T> getNodes() {
        return nodes;
    }
}
