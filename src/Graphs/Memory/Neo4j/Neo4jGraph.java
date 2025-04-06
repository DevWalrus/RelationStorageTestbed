package Graphs.Memory.Neo4j;

import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Neo4jGraph<T> implements IGraph<T> {
    private final Set<T> nodes = new HashSet<>();
    private final Map<T, Map<String, EdgeList<T>>> adjList = new HashMap<>();
    private final Random rand = ThreadLocalRandom.current();

    @Override
    public Iterable<T> getNodes() {
        return nodes;
    }

    @Override
    public void addNode(T node) {
        if (nodes.add(node)) {
            adjList.put(node, new HashMap<>());
        }
    }

    @Override
    public void addRelationship(String label, T source, T target) {
        addNode(source);
        addNode(target);
        Map<String, EdgeList<T>> map = adjList.get(source);
        EdgeList<T> list = map.computeIfAbsent(label, _ -> new EdgeList<>());
        list.add(new Edge<>(source, target, label));
    }

    @Override
    public Iterable<Edge<T>> getRelationships(T node) {
        var neighbors = new HashSet<Edge<T>>();
        var map = adjList.get(node);
        if (map != null) {
            for (EdgeList<T> list : map.values()) {
                for (Edge<T> edge : list) {
                    neighbors.add(edge);
                }
            }
        }
        return neighbors;
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) return null;
        int randomIndex = rand.nextInt(nodes.size());
        return new ArrayList<>(nodes).get(randomIndex);
    }

    @Override
    public Edge<T> getRandomRelationship(T node) {
        Map<String, EdgeList<T>> map = adjList.get(node);
        if (map == null) return null;
        Edge<T> chosenEdge = null;
        int count = 0;
        for (EdgeList<T> list : map.values()) {
            for (Edge<T> edge : list) {
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
        adjList.clear();
    }

    @Override
    public void close() {}
}
