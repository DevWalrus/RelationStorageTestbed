package Graphs.Memory.Neo4j;

import Graphs.Edge;
import Graphs.IGraph;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Neo4jGraph<T> implements IGraph<T> {
    // Use a map to store Neo4jNodes keyed by the node value.
    private final Map<T, Neo4jNode<T>> nodes = new HashMap<>();
    private static final Random rand = new Random(8675309);

    @Override
    public Iterator<T> getNodes() {
        return nodes.keySet().iterator();
    }

    @Override
    public void addNode(T nodeValue) {
        // Only add a new node if it doesn't already exist.
        nodes.computeIfAbsent(nodeValue, _ -> new Neo4jNode<>(nodeValue));
    }

    @Override
    public void addRelationship(String label, T source, T target) {
        // Ensure both source and target nodes exist.
        addNode(source);
        addNode(target);

        Neo4jNode<T> sourceNode = nodes.get(source);
        Neo4jNode<T> targetNode = nodes.get(target);

        // Create the edge and wrap it in an EdgeNode.
        Edge<T> newEdge = new Edge<>(source, target, label);
        EdgeNode<T> newEdgeNode = new EdgeNode<>(newEdge);

        // Insert into the source's outgoing edge list.
        newEdgeNode.outNext = sourceNode.getFirstOutgoing();
        if (sourceNode.getFirstOutgoing() != null) {
            sourceNode.getFirstOutgoing().outPrev = newEdgeNode;
        }
        sourceNode.setFirstOutgoing(newEdgeNode);

        // Insert into the target's incoming edge list.
        newEdgeNode.inNext = targetNode.getFirstIncoming();
        if (targetNode.getFirstIncoming() != null) {
            targetNode.getFirstIncoming().inPrev = newEdgeNode;
        }
        targetNode.setFirstIncoming(newEdgeNode);
    }

    @Override
    public Iterator<Edge<T>> getRelationships(T nodeValue) {
        Neo4jNode<T> memNode = nodes.get(nodeValue);
        if (memNode == null) {
            return Collections.emptyIterator();
        }
        // Here we collect both outgoing and incoming relationships.
        Set<Edge<T>> relationships = new HashSet<>();
        for (EdgeNode<T> curr = memNode.getFirstOutgoing(); curr != null; curr = curr.outNext) {
            relationships.add(curr.edge);
        }
        return relationships.iterator();
    }

    @Override
    public T getRandomNode() {
        if (nodes.isEmpty()) return null;
        List<T> keys = new ArrayList<>(nodes.keySet());
        int randomIndex = rand.nextInt(keys.size());
        return keys.get(randomIndex);
    }

    @Override
    public Edge<T> getRandomRelationship(T nodeValue) {
        Neo4jNode<T> memNode = nodes.get(nodeValue);
        if (memNode == null) {
            return null;
        }
        // Use reservoir sampling over both outgoing and incoming edges.
        int count = 0;
        Edge<T> chosenEdge = null;

        for (EdgeNode<T> curr = memNode.getFirstOutgoing(); curr != null; curr = curr.outNext) {
            count++;
            if (rand.nextInt(count) == 0) {
                chosenEdge = curr.edge;
            }
        }
        return chosenEdge;
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public void close() {}
}
