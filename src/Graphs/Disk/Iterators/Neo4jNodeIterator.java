package Graphs.Disk.Iterators;

import Graphs.Disk.Constants;
import Graphs.Disk.GraphRandomAccessFile;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Neo4jNodeIterator implements Iterator<Integer> {
    private final GraphRandomAccessFile raf;
    private final long nodeSize;
    private final int totalCount;
    private int nextNode;

    public Neo4jNodeIterator(GraphRandomAccessFile raf, long nodeSize) throws IOException {
        this.raf = raf;
        this.nodeSize = nodeSize;
        this.totalCount = raf.getCount();
        this.nextNode = 0;
    }

    @Override
    public boolean hasNext() {
        return nextNode < totalCount;
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        try {
            raf.seek(Constants.INT_SIZE + (nextNode * nodeSize));
            while (!raf.readBoolean()) {
                nextNode++;
                raf.skipBytes(nodeSize - Constants.BOOL_SIZE);
            }
            return nextNode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
