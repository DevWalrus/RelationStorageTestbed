package Graphs.Disk.Iterators;

import Graphs.Disk.GraphRandomAccessFile;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EdgeListNodeIterator implements Iterator<Integer> {
    private final GraphRandomAccessFile raf;
    private int remaining;

    public EdgeListNodeIterator(GraphRandomAccessFile raf) throws IOException {
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
            remaining--;
            return node;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
