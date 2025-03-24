package Graphs;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleGraph<T> implements IGraph<T> {
    private final Set<T> nodes = new HashSet<>();
    private final Map<T, List<Edge<T>>> adj = new HashMap<>();

    @Override
    public void addNode(T node) {
        nodes.add(node);
        adj.putIfAbsent(node, new ArrayList<>());
    }

    @Override
    public void removeNode(T node) {
        nodes.remove(node);
        adj.remove(node);
        for (List<Edge<T>> edges : adj.values()) {
            edges.removeIf(edge -> edge.getTarget().equals(node));
        }
    }

    @Override
    public void addRelationship(String label, T source, T target) {
        addNode(source);
        addNode(target);
        adj.get(source).add(new Edge<>(source, target, label));
        adj.get(target).add(new Edge<>(target, source, label));
    }

    @Override
    public void removeRelationship(String label, T source, T target) {
        List<Edge<T>> sourceEdges = adj.get(source);
        if (sourceEdges != null) {
            sourceEdges.removeIf(edge -> edge.getLabel().equals(label) && edge.getTarget().equals(target));
        }
        List<Edge<T>> targetEdges = adj.get(target);
        if (targetEdges != null) {
            targetEdges.removeIf(edge -> edge.getLabel().equals(label) && edge.getTarget().equals(source));
        }
    }

    @Override
    public Iterable<T> getRelationships(T node) {
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
    public Iterable<T> getRelationships(T node, String label) {
        List<T> result = new ArrayList<>();
        List<Edge<T>> edges = adj.get(node);
        if (edges != null) {
            for (Edge<T> edge : edges) {
                if (edge.getLabel().equals(label)) {
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
    public Edge<T> getRandomRelationship(T node) {
        List<Edge<T>> edges = adj.get(node);
        if (edges == null || edges.isEmpty()) return null;
        Random rand = ThreadLocalRandom.current();
        int count = 0;
        Edge<T> chosenEdge = null;
        for (Edge<T> edge : edges) {
            count++;
            if (rand.nextInt(count) == 0) {
                chosenEdge = edge;
            }
        }
        return chosenEdge;
    }

    @Override
    public Edge<T> getRandomRelationship(T node, String label) {
        List<Edge<T>> edges = adj.get(node);
        if (edges == null || edges.isEmpty()) return null;
        Random rand = ThreadLocalRandom.current();
        int count = 0;
        Edge<T> chosenEdge = null;
        for (Edge<T> edge : edges) {
            if (edge.getLabel().equals(label)) {
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
