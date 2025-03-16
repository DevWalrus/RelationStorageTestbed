package Graphs;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AdjMatrixCountGraph<T> implements IGraph<T> {
    // Maps each node to its index in the matrix.
    private final Map<T, Integer> nodeIndex = new HashMap<>();
    // List of nodes (to associate an index with each node).
    private final List<T> nodes = new ArrayList<>();
    // The matrix where cell (i, j) holds the set of Edge objects from node i to node j.
    private final List<List<Set<Edge<T>>>> matrix = new ArrayList<>();

    @Override
    public List<T> getNodes() {
        return nodes;
    }

    @Override
    public void addNode(T node) {
        if (nodeIndex.containsKey(node)) return;
        int index = nodes.size();
        nodes.add(node);
        nodeIndex.put(node, index);
        // Expand every existing row with a new empty cell.
        for (List<Set<Edge<T>>> row : matrix) {
            row.add(new HashSet<>());
        }
        // Create a new row for the new node.
        List<Set<Edge<T>>> newRow = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            newRow.add(new HashSet<>());
        }
        matrix.add(newRow);
    }

    @Override
    public void removeNode(T node) {
        Integer idx = nodeIndex.get(node);
        if (idx == null) return;
        int index = idx;
        nodes.remove(index);
        matrix.remove(index);
        for (List<Set<Edge<T>>> row : matrix) {
            row.remove(index);
        }
        nodeIndex.remove(node);
        // Update indices for nodes coming after the removed one.
        for (int i = index; i < nodes.size(); i++) {
            T n = nodes.get(i);
            nodeIndex.put(n, i);
        }
    }

    @Override
    public void addEdge(String label, T source, T target) {
        if (!nodeIndex.containsKey(source)) addNode(source);
        if (!nodeIndex.containsKey(target)) addNode(target);
        int srcIdx = nodeIndex.get(source);
        int tgtIdx = nodeIndex.get(target);
        Edge<T> newEdge = new Edge<>(source, target, label);
        matrix.get(srcIdx).get(tgtIdx).add(newEdge);
    }

    @Override
    public void removeEdge(String label, T source, T target) {
        Integer srcIdx = nodeIndex.get(source);
        Integer tgtIdx = nodeIndex.get(target);
        if (srcIdx == null || tgtIdx == null) return;
        matrix.get(srcIdx).get(tgtIdx).removeIf(edge -> edge.getLabel().equals(label));
    }

    @Override
    public Iterable<T> getNeighbors(T node) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return Collections.emptyList();
        List<T> neighbors = new ArrayList<>();
        List<Set<Edge<T>>> row = matrix.get(srcIdx);
        for (int j = 0; j < row.size(); j++) {
            if (!row.get(j).isEmpty()) {
                neighbors.add(nodes.get(j));
            }
        }
        return neighbors;
    }

    @Override
    public Iterable<T> getNeighbors(T node, String label) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return Collections.emptyList();
        List<T> neighbors = new ArrayList<>();
        List<Set<Edge<T>>> row = matrix.get(srcIdx);
        for (int j = 0; j < row.size(); j++) {
            Set<Edge<T>> cell = row.get(j);
            for (Edge<T> edge : cell) {
                if (edge.getLabel().equals(label)) {
                    neighbors.add(nodes.get(j));
                    break; // Only add the neighbor once.
                }
            }
        }
        return neighbors;
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) return null;
        int randomIndex = ThreadLocalRandom.current().nextInt(nodes.size());
        return nodes.get(randomIndex);
    }

    @Override
    public Edge<T> getRandomEdge(T node) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return null;
        List<Edge<T>> candidateEdges = new ArrayList<>();
        List<Set<Edge<T>>> row = matrix.get(srcIdx);
        for (Set<Edge<T>> cell : row) {
            candidateEdges.addAll(cell);
        }
        if (candidateEdges.isEmpty()) return null;
        int randomIndex = ThreadLocalRandom.current().nextInt(candidateEdges.size());
        return candidateEdges.get(randomIndex);
    }

    @Override
    public Edge<T> getRandomEdge(T node, String label) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return null;
        List<Edge<T>> candidateEdges = new ArrayList<>();
        List<Set<Edge<T>>> row = matrix.get(srcIdx);
        for (Set<Edge<T>> cell : row) {
            for (Edge<T> edge : cell) {
                if (edge.getLabel().equals(label)) {
                    candidateEdges.add(edge);
                }
            }
        }
        if (candidateEdges.isEmpty()) return null;
        int randomIndex = ThreadLocalRandom.current().nextInt(candidateEdges.size());
        return candidateEdges.get(randomIndex);
    }
}
