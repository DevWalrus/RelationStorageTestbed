package Graphs.Memory;

import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;

public class AdjMatrixGraph<T> implements IGraph<T> {
    protected final Map<T, Integer> nodeIndex = new HashMap<>();
    protected final List<T> nodes = new ArrayList<>();
    protected final List<List<Set<Edge<T>>>> matrix = new ArrayList<>();
    protected static final Random rand = new Random(8675309);

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
    public void addRelationship(String label, T source, T target) {
        if (!nodeIndex.containsKey(source)) addNode(source);
        if (!nodeIndex.containsKey(target)) addNode(target);
        int srcIdx = nodeIndex.get(source);
        int tgtIdx = nodeIndex.get(target);
        Edge<T> newEdge = new Edge<>(source, target, label);
        matrix.get(srcIdx).get(tgtIdx).add(newEdge);
    }

    @Override
    public Iterable<Edge<T>> getRelationships(T node) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return Collections.emptyList();

        var neighbors = new ArrayList<Edge<T>>();

        List<Set<Edge<T>>> row = matrix.get(srcIdx);
        for (var edges : row) {
            neighbors.addAll(edges);
        }
        return neighbors;
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) return null;
        int randomIndex = rand.nextInt(nodes.size());
        return nodes.get(randomIndex);
    }

    @Override
    public Edge<T> getRandomRelationship(T node) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return null;
        int count = 0;
        Edge<T> chosenEdge = null;
        List<Set<Edge<T>>> row = matrix.get(srcIdx);
        for (Set<Edge<T>> cell : row) {
            for (Edge<T> edge : cell) {
                count++;
                if (rand.nextInt(count) == 0) {
                    chosenEdge = edge;
                }
            }
        }
        return chosenEdge;
    }

    @Override
    public void clear() {
        nodes.clear();
        nodeIndex.clear();
        matrix.clear();
    }

    @Override
    public void close() {}
}
