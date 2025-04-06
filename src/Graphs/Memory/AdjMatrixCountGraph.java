package Graphs.Memory;

import Graphs.Edge;

import java.util.*;

public class AdjMatrixCountGraph<T> extends AdjMatrixGraph<T> {
    @Override
    public Edge<T> getRandomRelationship(T node) {
        Integer srcIdx = nodeIndex.get(node);
        if (srcIdx == null) return null;
        List<Edge<T>> candidateEdges = new ArrayList<>();
        List<Set<Edge<T>>> row = matrix.get(srcIdx);
        for (Set<Edge<T>> cell : row) {
            candidateEdges.addAll(cell);
        }
        if (candidateEdges.isEmpty()) return null;
        int randomIndex = rand.nextInt(candidateEdges.size());
        return candidateEdges.get(randomIndex);
    }
}
