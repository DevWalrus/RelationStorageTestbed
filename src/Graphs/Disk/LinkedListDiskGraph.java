package Graphs.Disk;

import Exceptions.InvalidNodeAccessException;
import Graphs.Disk.Iterators.FastRandomNodeIterator;
import Graphs.Disk.Iterators.FastRandomRelationshipIterator;
import Graphs.Edge;
import Graphs.IGraph;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LinkedListDiskGraph implements IGraph<Integer>, AutoCloseable {
    private final GraphRandomAccessFile nodesRaf;
    private final GraphRandomAccessFile edgesRaf;
    private static final Random rand = ThreadLocalRandom.current();

    public LinkedListDiskGraph(String directoryPath) throws IOException {
        this(directoryPath, "nodes.dat", "edges.dat");
    }

    public LinkedListDiskGraph(String directoryPath, String nodesFileName, String edgesFileName) throws IOException {
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
            nodesRaf.skipBytes(Constants.LONG_SIZE);
            if (n == node) {
                return true;
            }
        }
        return false;
    }

    private long getNeighborPointer(Integer node) throws IOException, InvalidNodeAccessException {
        nodesRaf.seek(Constants.INT_SIZE); // Skip the count
        while (nodesRaf.getFilePointer() < nodesRaf.length()) {
            int n = nodesRaf.readInt();
            if (n == node) {
                return nodesRaf.readLong();
            }
            nodesRaf.skipBytes(Constants.LONG_SIZE);
        }
        throw new InvalidNodeAccessException("The node '" + node + "' is not in the graph.");
    }

    private void addNeighborPointer(Integer node, long pos) throws IOException, InvalidNodeAccessException {
        nodesRaf.seek(Constants.INT_SIZE); // Skip the count
        while (nodesRaf.getFilePointer() < nodesRaf.length()) {
            int n = nodesRaf.readInt();
            long nodeRafPos = nodesRaf.getFilePointer();
            if (n == node) {
                var edgeFP = nodesRaf.readLong();
                if (edgeFP == -1) {
                    nodesRaf.seek(nodeRafPos);
                    nodesRaf.writeLong(pos);
                } else {
                    var possibleWritePos = -1L;
                    while (edgeFP != -1L) {
                        edgesRaf.seek(edgeFP);
                        edgesRaf.skipBytes(Constants.INT_SIZE);
                        possibleWritePos = edgesRaf.getFilePointer();
                        edgeFP = edgesRaf.readLong();
                    }
                    edgesRaf.seek(possibleWritePos);
                    edgesRaf.writeLong(pos);
                }
                return;
            }
            nodesRaf.skipBytes(Constants.LONG_SIZE);
        }
        throw new InvalidNodeAccessException("The node '" + node + "' is not in the graph.");
    }

    @Override
    public void addNode(Integer node) throws IOException {
        if (nodeExists(node)) {
            return;
        }
        nodesRaf.seekTheEnd();
        nodesRaf.writeInt(node); // id
        nodesRaf.writeLong(-1); // pointer to first edge
        nodesRaf.incCount();
    }

    @Override
    public Iterable<Integer> getNodes() {
        return () -> {
            try {
                return new FastRandomNodeIterator(nodesRaf);
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
        long offset = Constants.INT_SIZE + randomIndex * (Constants.INT_SIZE + Constants.LONG_SIZE);
        nodesRaf.seek(offset);
        return nodesRaf.readInt();
    }

    @Override
    public void addRelationship(String label, Integer source, Integer target) throws InvalidNodeAccessException, IOException {
        edgesRaf.seekTheEnd();
        var addedFP = edgesRaf.getFilePointer();
        edgesRaf.writeInt(target);
        edgesRaf.writeLong(-1L);
        addNeighborPointer(source, addedFP);
        edgesRaf.incCount();
    }

    @Override
    public Iterable<Edge<Integer>> getRelationships(Integer node) throws InvalidNodeAccessException, IOException {
        var startingPos = getNeighborPointer(node);
        return () -> {
            try {
                return new FastRandomRelationshipIterator(node, startingPos, edgesRaf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public Edge<Integer> getRandomRelationship(Integer node) throws InvalidNodeAccessException, IOException {
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

