package Graphs.Disk.Neo4j;

import Exceptions.InvalidNodeAccessException;
import Graphs.Disk.Constants;
import Graphs.Disk.GraphRandomAccessFile;
import Graphs.Disk.Iterators.Neo4jNodeIterator;
import Graphs.Disk.Iterators.Neo4jRelationshipIterator;
import Graphs.Edge;
import Graphs.IGraph;

import java.io.*;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Neo4jDiskGraph implements IGraph<Integer>, AutoCloseable {
    private final GraphRandomAccessFile<Neo4jNode> nodesRaf;
    private final GraphRandomAccessFile<Neo4jEdge> edgesRaf;
    private static final Random rand = ThreadLocalRandom.current();

    public Neo4jDiskGraph(String directoryPath) throws IOException {
        this(directoryPath, "nodes.dat", "edges.dat");
    }

    public Neo4jDiskGraph(String directoryPath, String nodesFileName, String edgesFileName) throws IOException {
        File dir = new File(directoryPath);
        File nodesFile = new File(dir, nodesFileName);
        nodesRaf = new GraphRandomAccessFile<>(nodesFile);

        File edgesFile = new File(dir, edgesFileName);
        edgesRaf = new GraphRandomAccessFile<>(edgesFile);
    }

    private Neo4jNode getExistingNode(Integer nodeId) throws IOException {
        // Assume the first 4 bytes of nodesRaf store a count; records start immediately after.
        long offset = Constants.INT_SIZE + (nodeId * Neo4jNode.RECORD_SIZE);
        if (offset >= nodesRaf.length()) {
            return null;
        }
        nodesRaf.seek(offset);
        Neo4jNode foundNode = nodesRaf.readElement(Neo4jNode::new);
        return foundNode.isInUse() ? foundNode : null;
    }

    // Check if a node record exists at the given node id by seeking to its record
    private boolean nodeExists(Integer nodeId) throws IOException {
        return getExistingNode(nodeId) != null;
    }

    @Override
    public void addNode(Integer nodeId) throws IOException {
        // In a Neo4j-like store, the node id is implicit from its position.
        if (nodeExists(nodeId)) {
            return;
        }

        Neo4jNode newNode = new Neo4jNode(true, -1, -1);
        long offset = Constants.INT_SIZE + (nodeId * Neo4jNode.RECORD_SIZE);
        nodesRaf.seek(offset);
        nodesRaf.writeElement(newNode);

        nodesRaf.incCount();
    }


    private void updateNode(Neo4jNode node, int nodeId) throws IOException {
        long offset = Constants.INT_SIZE + (nodeId * Neo4jNode.RECORD_SIZE);
        nodesRaf.seek(offset);
        nodesRaf.writeElement(node);
    }

    @Override
    public void addRelationship(String label, Integer source, Integer target)
            throws InvalidNodeAccessException, IOException {
        var sourceNode = getExistingNode(source);
        if (sourceNode == null) {
            throw new InvalidNodeAccessException("The source node is not in the graph.");
        }
        var targetNode = getExistingNode(target);
        if (targetNode == null) {
            throw new InvalidNodeAccessException("The target node is not in the graph.");
        }

        long currentSourceOutgoing = sourceNode.getOutgoingPointer();
        long currentTargetIncoming = targetNode.getIncomingPointer();
        boolean isChainHead = (currentSourceOutgoing == -1 && currentTargetIncoming == -1);
        Neo4jEdge newEdge = new Neo4jEdge(source, target, currentSourceOutgoing, -1, currentTargetIncoming, -1, isChainHead);

        edgesRaf.seekTheEnd();
        long newRelPos = edgesRaf.getFilePointer();
        edgesRaf.writeElement(newEdge);

        // Update node records:
        // For the source, update the outgoing chain to point to the new relationship.
        sourceNode.setOutgoingPointer(newRelPos);
        updateNode(sourceNode, source);

        // For the target, update the incoming chain.
        if (currentTargetIncoming == -1) {
            targetNode.setIncomingPointer(newRelPos);
            updateNode(targetNode, target);
        } else {
            // If there is already an incoming chain, update the previous pointer
            // of the current head of the incoming chain to point to the new relationship.
            edgesRaf.seek(currentTargetIncoming);
            var prevEdge = edgesRaf.readElement(Neo4jEdge::new);
            prevEdge.setIncomingPrev(newRelPos);
            edgesRaf.seek(currentTargetIncoming);
            edgesRaf.writeElement(prevEdge);

        }

        edgesRaf.incCount();
    }

    @Override
    public Iterator<Edge<Integer>> getRelationships(Integer node) throws IOException {
        return new Neo4jRelationshipIterator(node, nodesRaf, edgesRaf);
    }

    @Override
    public Edge<Integer> getRandomRelationship(Integer node) throws IOException {
        int count = 0;
        Edge<Integer> chosenEdge = null;
        for (Iterator<Edge<Integer>> it = getRelationships(node); it.hasNext(); ) {
            Edge<Integer> edge = it.next();
            count++;
            if (rand.nextInt(count) == 0) {
                chosenEdge = edge;
            }
        }
        return chosenEdge;
    }

    @Override
    public Iterator<Integer> getNodes() throws IOException {
        return new Neo4jNodeIterator(nodesRaf, Neo4jNode.RECORD_SIZE);
    }

    @Override
    public Integer getRandomNode() throws IOException {
        int count = nodesRaf.getCount();
        if (count <= 0) {
            throw new IOException("No nodes available in the graph.");
        }
        int randomIndex = rand.nextInt(count);
        long offset = Constants.INT_SIZE + (randomIndex * Neo4jNode.RECORD_SIZE);
        nodesRaf.seek(offset);
        boolean inUse = nodesRaf.readBoolean();
        while (!inUse) {
            randomIndex++; // rand wasn't active, inc until it is

            offset = Constants.INT_SIZE + (randomIndex * Neo4jNode.RECORD_SIZE);
            if (offset > nodesRaf.length()) offset = Constants.INT_SIZE; // Wrap around if needed
            nodesRaf.seek(offset);

            inUse = nodesRaf.readBoolean();
        }
        return randomIndex;
    }

    @Override
    public void close() throws Exception {
        nodesRaf.close();
        edgesRaf.close();
    }

    @Override
    public void clear() throws IOException {
        nodesRaf.clear();
        edgesRaf.clear();
    }
}
