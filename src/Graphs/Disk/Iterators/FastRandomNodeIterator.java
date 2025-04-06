package Graphs.Disk.Iterators;

import Graphs.Disk.Constants;
import Graphs.Disk.GraphRandomAccessFile;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FastRandomNodeIterator implements Iterator<Integer> {
    private final GraphRandomAccessFile raf;
    private int remaining;

    public FastRandomNodeIterator(GraphRandomAccessFile raf) throws IOException {
        this.raf = raf;
        this.remaining = raf.getCount();
    }

    @Override
    public boolean hasNext() {
        return remaining > 0;
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int node;
        try {
            node = raf.readInt();
            raf.skipBytes(Constants.LONG_SIZE);
            remaining--;
            return node;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
