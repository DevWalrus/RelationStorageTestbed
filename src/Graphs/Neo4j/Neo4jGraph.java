package Graphs.Neo4j;

import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Neo4jGraph<T> implements IGraph<T> {
    // Set of nodes.
    private final Set<T> nodes = new HashSet<>();
    // For each node, a map from edge label to a linked list (EdgeList) of edges.
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
    public void removeNode(T node) {
        if (nodes.remove(node)) {
            // Remove outgoing edges.
            adjList.remove(node);
            // Remove incoming edges: for every node, remove all edges whose target equals the removed node.
            for (Map<String, EdgeList<T>> map : adjList.values()) {
                for (EdgeList<T> list : map.values()) {
                    list.removeIfTargetEquals(node);
                }
            }
        }
    }

    @Override
    public void addEdge(String label, T source, T target) {
        addNode(source);
        addNode(target);
        Map<String, EdgeList<T>> map = adjList.get(source);
        EdgeList<T> list = map.get(label);
        if (list == null) {
            list = new EdgeList<>();
            map.put(label, list);
        }
        list.add(new Edge<>(source, target, label));
    }

    @Override
    public void removeEdge(String label, T source, T target) {
        Map<String, EdgeList<T>> map = adjList.get(source);
        if (map == null) return;
        EdgeList<T> list = map.get(label);
        if (list != null) {
            list.remove(label, target);
            if (list.isEmpty()) {
                map.remove(label);
            }
        }
    }

    @Override
    public Iterable<T> getNeighbors(T node) {
        Set<T> neighbors = new HashSet<>();
        Map<String, EdgeList<T>> map = adjList.get(node);
        if (map != null) {
            for (EdgeList<T> list : map.values()) {
                for (Edge<T> edge : list) {
                    neighbors.add(edge.getTarget());
                }
            }
        }
        return neighbors;
    }

    @Override
    public Iterable<T> getNeighbors(T node, String label) {
        Map<String, EdgeList<T>> map = adjList.get(node);
        if (map == null) return Collections.emptyList();
        EdgeList<T> list = map.get(label);
        Set<T> neighbors = new HashSet<>();
        if (list != null) {
            for (Edge<T> edge : list) {
                neighbors.add(edge.getTarget());
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
    public Edge<T> getRandomEdge(T node) {
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
    public Edge<T> getRandomEdge(T node, String label) {
        Map<String, EdgeList<T>> map = adjList.get(node);
        if (map == null) return null;
        EdgeList<T> list = map.get(label);
        if (list == null || list.isEmpty()) return null;
        return list.getRandomEdge(rand);
    }
}
