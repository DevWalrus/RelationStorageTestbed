package Graphs.Disk;

import Exceptions.InvalidNodeAccessException;
import Graphs.Disk.Iterators.EdgeListNodeIterator;
import Graphs.Disk.Iterators.EdgeListRelationshipIterator;
import Graphs.Edge;
import Graphs.IGraph;

import java.io.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EdgeListDiskGraph implements IGraph<Integer>, AutoCloseable {
    private final GraphRandomAccessFile nodesRaf;
    private final GraphRandomAccessFile edgesRaf;
    private static final Random rand = ThreadLocalRandom.current();

    public EdgeListDiskGraph(String directoryPath) throws IOException {
        this(directoryPath, "nodes.dat", "edges.dat");
    }

    public EdgeListDiskGraph(String directoryPath, String nodesFileName, String edgesFileName) throws IOException {
        File dir = new File(directoryPath);
        File nodesFile = new File(dir, nodesFileName);
        nodesRaf = new GraphRandomAccessFile(nodesFile);

        File edgesFile = new File(dir, edgesFileName);
        edgesRaf = new GraphRandomAccessFile(edgesFile);
    }

    private boolean nodeExists(Integer node) throws IOException {
        nodesRaf.seek(Constants.INT_SIZE); // Skip the count
        while (nodesRaf.getFilePointer() < nodesRaf.length()) {
            int n = nodesRaf.readInt();
            if (n == node) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addNode(Integer node) throws IOException {
        if (nodeExists(node)) {
            return;
        }
        nodesRaf.seekTheEnd();
        nodesRaf.writeInt(node);
        nodesRaf.incCount();
    }

    @Override
    public void addRelationship(String label, Integer source, Integer target) throws InvalidNodeAccessException, IOException {
        if (!nodeExists(source)) {
            throw new InvalidNodeAccessException("The source node is not in the graph.");
        }
        if (!nodeExists(target)) {
            throw new InvalidNodeAccessException("The target node is not in the graph.");
        }

        edgesRaf.seekTheEnd();
        edgesRaf.writeInt(source);
        edgesRaf.writeInt(target);
        edgesRaf.writeUTF(label);
        edgesRaf.incCount();
    }

    @Override
    public Iterable<Edge<Integer>> getRelationships(Integer node) {
        return () -> {
            try {
                return new EdgeListRelationshipIterator(node, edgesRaf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public Edge<Integer> getRandomRelationship(Integer node) {
        int count = 0;
        Edge<Integer> chosenEdge = null;
        for (Edge<Integer> edge : getRelationships(node)) {
            count++;
            if (rand.nextInt(count) == 0) {
                chosenEdge = edge;
            }
        }
        return chosenEdge;
    }

    @Override
    public Iterable<Integer> getNodes() {
        return () -> {
            try {
                return new EdgeListNodeIterator(nodesRaf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public Integer getRandomNode() throws IOException {
        int count = nodesRaf.getCount();
        if (count <= 0) {
            throw new IOException("No nodes available in the graph.");
        }
        int randomIndex = rand.nextInt(count);
        // Skip the first 4 bytes (the count) and then each node is 4 bytes.
        long offset = Constants.INT_SIZE + randomIndex * Constants.INT_SIZE;
        nodesRaf.seek(offset);
        return nodesRaf.readInt();
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

