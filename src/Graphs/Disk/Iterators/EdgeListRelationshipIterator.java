package Graphs.Disk.Iterators;

import Graphs.Disk.GraphRandomAccessFile;
import Graphs.Edge;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EdgeListRelationshipIterator implements Iterator<Edge<Integer>> {
    private final int node;
    private final GraphRandomAccessFile raf;
    private final int totalRelationships;
    private int currentRelationshipIndex;
    private Edge<Integer> nextEdge;

    public EdgeListRelationshipIterator(int node, GraphRandomAccessFile raf) throws IOException {
        this.node = node;
        this.raf = raf;
        // Read the total number of relationships from the header.
        raf.seek(0);
        this.totalRelationships = raf.readInt();
        // Set the pointer to the first relationship entry (skip the count header).
        raf.seek(4);
        this.currentRelationshipIndex = 0;
        this.nextEdge = null;
    }

    /**
     * Advances the file pointer until a relationship that involves the given node is found.
     * Sets nextOtherNode to the neighboring node if found.
     */
    private void advance() throws IOException {
        nextEdge = null;
        while (currentRelationshipIndex < totalRelationships && nextEdge == null) {
            // Each relationship entry consists of two ints and a UTF string.
            int s = raf.readInt();
            int t = raf.readInt();
            currentRelationshipIndex++;
            if (node == s) {
                nextEdge = new Edge<>(s, t, "default");
            }
        }
    }

    @Override
    public boolean hasNext() {
        try {
            if (nextEdge == null) {
                advance();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading relationships", e);
        }
        return nextEdge != null;
    }

    @Override
    public Edge<Integer> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        var result = nextEdge;
        nextEdge = null;
        return result;
    }
}
