package Graphs.Disk.Iterators;

import Graphs.Disk.GraphRandomAccessFile;
import Graphs.Disk.LinkedList.LinkedListEdge;
import Graphs.Disk.LinkedList.LinkedListNode;
import Graphs.Edge;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListRelationshipIterator implements Iterator<Edge<Integer>> {
    private final int node;
    private final GraphRandomAccessFile<LinkedListEdge> raf;
    private long nextPos;

    public LinkedListRelationshipIterator(int node, long startingPos, GraphRandomAccessFile<LinkedListEdge> raf) throws IOException {
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
            LinkedListEdge edgeElem = raf.readElement(LinkedListEdge::new);
            nextPos = edgeElem.getNextNeighborPointer();
            return new Edge<>(node, edgeElem.getTargetNode(), "default");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
