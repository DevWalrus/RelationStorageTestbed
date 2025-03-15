package Graphs;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Neo4jGraph<T> implements IGraph<T> {
    private final Set<T> nodes = new HashSet<>();;
    private final Map<T, Map<String, Set<T>>> adjList = new HashMap<>();

    @Override
    public Set<T> getNodes() {
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
            // Remove the node's own adjacency list.
            adjList.remove(node);
            // Remove this node from all other nodes' relationship lists.
            for (Map<String, Set<T>> relMap : adjList.values()) {
                for (Set<T> neighbors : relMap.values()) {
                    neighbors.remove(node);
                }
            }
        }
    }

    @Override
    public void addEdge(String type, T source, T target) {
        addNode(source);
        addNode(target);
        Map<String, Set<T>> relMap = adjList.get(source);
        relMap.computeIfAbsent(type, _ -> new HashSet<>()).add(target);
    }

    @Override
    public void removeEdge(String type, T source, T target) {
        Map<String, Set<T>> relMap = adjList.get(source);
        if (relMap != null) {
            Set<T> neighbors = relMap.get(type);
            if (neighbors != null) {
                neighbors.remove(target);
                if (neighbors.isEmpty()) {
                    relMap.remove(type);
                }
            }
        }
    }

    @Override
    public Iterable<T> getNeighbors(T node) {
        Set<T> result = new HashSet<>();
        Map<String, Set<T>> relMap = adjList.get(node);
        if (relMap != null) {
            for (Set<T> neighbors : relMap.values()) {
                result.addAll(neighbors);
            }
        }
        return result;
    }

    @Override
    public Iterable<T> getNeighbors(T node, String type) {
        Map<String, Set<T>> relMap = adjList.get(node);
        if (relMap == null) return null;

        return relMap.getOrDefault(type, Collections.emptySet());
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(nodes.size());
        return nodes.stream().skip(randomIndex).findFirst().orElse(null);
    }

    @Override
    public Edge<T> getRandomEdge(T node) {
        Random rand = ThreadLocalRandom.current();
        Map<String, Set<T>> relMap = adjList.get(node);

        int count = 0;
        T chosenNode = null;

        if (relMap != null) {
            for (Set<T> neighbors : relMap.values()) {
                for (T neighbor : neighbors) {
                    count++;
                    if (rand.nextInt(count) == 0) {
                        chosenNode = neighbor;
                    }
                }
            }
        } else {
            return null; // No neighbors
        }

        return new Edge<>(node, chosenNode);
    }

    @Override
    public Edge<T> getRandomEdge(T node, String type) {
        Random rand = ThreadLocalRandom.current();

        Map<String, Set<T>> relMap = adjList.get(node);
        if (relMap == null) return null;

        Set<T> neiMap = relMap.get(type);
        if (neiMap == null) return null;

        int count = 0;
        T chosenNode = null;

        for (T neighbor : neiMap) {
            count++;
            if (rand.nextInt(count) == 0) {
                chosenNode = neighbor;
            }
        }

        return new Edge<>(node, chosenNode);
    }
}
