package Graphs;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AdjMatrixGraph<T> implements IGraph<T> {
    // Maps each node to its index in the matrix.
    private final Map<T, Integer> nodeIndex = new HashMap<>();
    // List of nodes (to associate an index with each node).
    private final List<T> nodes = new ArrayList<>();
    // The matrix where cell (i, j) holds the set of edge types from node i to node j.
    private final List<List<Set<String>>> matrix = new ArrayList<>();

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
        for (List<Set<String>> row : matrix) {
            row.add(new HashSet<>());
        }
        // Create a new row for the new node.
        List<Set<String>> newRow = new ArrayList<>();
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
        for (List<Set<String>> row : matrix) {
            row.remove(index);
        }
        nodeIndex.remove(node);
        for (int i = index; i < nodes.size(); i++) {
            T n = nodes.get(i);
            nodeIndex.put(n, i);
        }
    }

    @Override
    public void addEdge(String type, T source, T target) {
        if (!nodeIndex.containsKey(source)) addNode(source);
        if (!nodeIndex.containsKey(target)) addNode(target);
        int srcIdx = nodeIndex.get(source);
        int tgtIdx = nodeIndex.get(target);
        matrix.get(srcIdx).get(tgtIdx).add(type);    }

    @Override
    public void removeEdge(String type, T source, T target) {
        Integer srcIdx = nodeIndex.get(source);
        Integer tgtIdx = nodeIndex.get(target);
        if (srcIdx == null || tgtIdx == null) return;
        matrix.get(srcIdx).get(tgtIdx).remove(type);
    }

    @Override
    public Iterable<T> getNeighbors(T node) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return Collections.emptyList();
        List<T> neighbors = new ArrayList<>();
        List<Set<String>> row = matrix.get(srcIdx);
        for (int j = 0; j < row.size(); j++) {
            if (!row.get(j).isEmpty()) {
                neighbors.add(nodes.get(j));
            }
        }
        return neighbors;
    }

    @Override
    public Iterable<T> getNeighbors(T node, String type) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return Collections.emptyList();
        List<T> neighbors = new ArrayList<>();
        List<Set<String>> row = matrix.get(srcIdx);
        for (int j = 0; j < row.size(); j++) {
            if (row.get(j).contains(type)) {
                neighbors.add(nodes.get(j));
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

        Random rand = ThreadLocalRandom.current();
        int count = 0;
        T chosenTarget = null;

        List<Set<String>> row = matrix.get(srcIdx);
        for (int j = 0; j < row.size(); j++) {
            if (!row.get(j).isEmpty()) {
                count++;
                if (rand.nextInt(count) == 0) {
                    chosenTarget = nodes.get(j);
                }
            }
        }

        return (chosenTarget != null) ? new Edge<>(node, chosenTarget) : null;
    }

    @Override
    public Edge<T> getRandomEdge(T node, String type) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return null;
        Random rand = ThreadLocalRandom.current();
        int count = 0;
        T chosenTarget = null;
        List<Set<String>> row = matrix.get(srcIdx);
        for (int j = 0; j < row.size(); j++) {
            if (row.get(j).contains(type)) {
                count++;
                if (rand.nextInt(count) == 0) {
                    chosenTarget = nodes.get(j);
                }
            }
        }
        return (chosenTarget != null) ? new Edge<>(node, chosenTarget) : null;
    }
}
