package Graphs.Disk.Iterators;

import Graphs.Disk.GraphRandomAccessFile;
import Graphs.Edge;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FastRandomRelationshipIterator implements Iterator<Edge<Integer>> {
    private final int node;
    private final GraphRandomAccessFile raf;
    private long nextPos;

    public FastRandomRelationshipIterator(int node, long startingPos, GraphRandomAccessFile raf) throws IOException {
        this.node = node;
        this.raf = raf;
        this.nextPos = startingPos;
    }

    @Override
    public boolean hasNext() {
        return nextPos != -1;
    }

    @Override
    public Edge<Integer> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        try {
            raf.seek(nextPos);
            var target = raf.readInt();
            nextPos = raf.readLong();
            return new Edge<>(node, target, "default");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
