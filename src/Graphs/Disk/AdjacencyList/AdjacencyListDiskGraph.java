package Graphs.Disk.AdjacencyList;

import Exceptions.InvalidNodeAccessException;
import Graphs.Disk.Constants;
import Graphs.Disk.GraphRandomAccessFile;
import Graphs.Disk.Iterators.LinkedListNodeIterator;
import Graphs.Disk.Iterators.LinkedListRelationshipIterator;
import Graphs.Edge;
import Graphs.IGraph;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AdjacencyListDiskGraph implements IGraph<Integer>, AutoCloseable {
    private final GraphRandomAccessFile<AdjacencyListNode> nodesRaf;
    private final GraphRandomAccessFile<AdjacencyListEdge> edgesRaf;
    private static final Random rand = ThreadLocalRandom.current();

    public AdjacencyListDiskGraph(String directoryPath) throws IOException {
        this(directoryPath, "nodes.dat", "edges.dat");
    }

    public AdjacencyListDiskGraph(String directoryPath, String nodesFileName, String edgesFileName) throws IOException {
        File dir = new File(directoryPath);
        File nodesFile = new File(dir, nodesFileName);
        nodesRaf = new GraphRandomAccessFile<>(nodesFile);

        File edgesFile = new File(dir, edgesFileName);
        edgesRaf = new GraphRandomAccessFile<>(edgesFile);
    }

    private AdjacencyListNode getNode(Integer node) throws IOException {
        long offset = Constants.INT_SIZE + (node * AdjacencyListNode.RECORD_SIZE);
        if (offset >= nodesRaf.length()) {
            return null;
        }
        nodesRaf.seek(offset);
        AdjacencyListNode nodeElem = nodesRaf.readElement(AdjacencyListNode::new);
        if (nodeElem.isInUse()) {
            return nodeElem;
        }
        return null;
    }

    private boolean nodeExists(Integer node) throws IOException {
        return getNode(node) != null;
    }

    private long getNeighborPointer(Integer node) throws IOException {
        long offset = Constants.INT_SIZE + (node * AdjacencyListNode.RECORD_SIZE);
        nodesRaf.seek(offset);
        return nodesRaf.readElement(AdjacencyListNode::new).getNeighborPointer();
    }

    private void updateNeighborPointer(AdjacencyListNode source, long newNeighborPos) throws IOException {
        source.setNeighborPointer(newNeighborPos);
        long offset = Constants.INT_SIZE + (source.getNodeId() * AdjacencyListNode.RECORD_SIZE);
        nodesRaf.seek(offset);
        nodesRaf.writeElement(source);
    }

    @Override
    public void addNode(Integer node) throws IOException {
        if (nodeExists(node)) {
            return;
        }
        long offset = Constants.INT_SIZE + (node * AdjacencyListNode.RECORD_SIZE);
        AdjacencyListNode nodeElem = new AdjacencyListNode(true, node, -1);
        nodesRaf.seek(offset);
        nodesRaf.writeElement(nodeElem);
        nodesRaf.incCount();
    }

    @Override
    public Iterator<Integer> getNodes() throws IOException {
        return new LinkedListNodeIterator(nodesRaf);
    }

    @Override
    public Integer getRandomNode() throws IOException {
        int count = nodesRaf.getCount();
        if (count <= 0) {
            throw new IOException("No nodes available in the graph.");
        }
        int randomIndex = rand.nextInt(count);
        long offset = Constants.INT_SIZE + (randomIndex * AdjacencyListNode.RECORD_SIZE);
        nodesRaf.seek(offset);
        boolean inUse = nodesRaf.readBoolean();
        while (!inUse) {
            randomIndex++; // rand wasn't active, inc until it is

            offset = Constants.INT_SIZE + (randomIndex * AdjacencyListNode.RECORD_SIZE);
            if (offset > nodesRaf.length()) offset = Constants.INT_SIZE; // Wrap around if needed
            nodesRaf.seek(offset);

            inUse = nodesRaf.readBoolean();
        }
        return randomIndex;
    }

    @Override
    public void addRelationship(String label, Integer source, Integer target) throws InvalidNodeAccessException, IOException {
        long offset = Constants.INT_SIZE + (source * AdjacencyListNode.RECORD_SIZE);
        nodesRaf.seek(offset);
        AdjacencyListNode sourceElem = nodesRaf.readElement(AdjacencyListNode::new);
        if (sourceElem == null) {
            throw new InvalidNodeAccessException("The source node is not in the graph.");
        }
        if (!nodeExists(target)) {
            throw new InvalidNodeAccessException("The target node is not in the graph.");
        }

        AdjacencyListEdge edgeElem = new AdjacencyListEdge(target, sourceElem.getNeighborPointer());

        edgesRaf.seekTheEnd();
        var addedFP = edgesRaf.getFilePointer();
        edgesRaf.writeElement(edgeElem);
        updateNeighborPointer(sourceElem, addedFP);
        edgesRaf.incCount();
    }

    @Override
    public Iterator<Edge<Integer>> getRelationships(Integer node) throws InvalidNodeAccessException, IOException {
        if (!nodeExists(node)) {
            throw new InvalidNodeAccessException("The node is not in the graph.");
        }
        var startingPos = getNeighborPointer(node);
        return new LinkedListRelationshipIterator(node, startingPos, edgesRaf);
    }

    @Override
    public Edge<Integer> getRandomRelationship(Integer node) throws InvalidNodeAccessException, IOException {
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

