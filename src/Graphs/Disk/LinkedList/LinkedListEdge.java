package Graphs.Disk.LinkedList;

import Graphs.Disk.Constants;
import Graphs.Disk.GraphRecord;

import java.nio.ByteBuffer;

public class LinkedListEdge implements GraphRecord {
    public static final long RECORD_SIZE = (
        Constants.INT_SIZE +    // Target Node
        Constants.LONG_SIZE     // The next neighbor
    );

    private long nextNeighborPointer;
    private int targetNode;

    // Constructors
    public LinkedListEdge() {
        this(-1, -1);
    }

    public LinkedListEdge(int target, long nextNeighborPointer) {
        this.nextNeighborPointer = nextNeighborPointer;
        this.targetNode = target;
    }

    // Getters and setters
    public long getNextNeighborPointer() {
        return nextNeighborPointer;
    }

    public void setNextNeighborPointer(int nextNeighborPointer) {
        this.nextNeighborPointer = nextNeighborPointer;
    }

    public int getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(int targetNode) {
        this.targetNode = targetNode;
    }

    @Override
    public int getRecordSize() {
        return (int) RECORD_SIZE;
    }

    /**
     * Converts this Edge record to a byte array.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getRecordSize());
        buffer.putInt(targetNode);
        buffer.putLong(nextNeighborPointer);
        return buffer.array();
    }

    /**
     * Populates this Edge from the given byte array.
     */
    @Override
    public void fromBytes(byte[] bytes) {
        if (bytes.length != getRecordSize()) {
            throw new IllegalArgumentException("Invalid byte array length for Edge");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        targetNode = buffer.getInt();
        nextNeighborPointer = buffer.getLong();
    }

    @Override
    public String toString() {
        return "Edge{" +
            "target=" + targetNode +
            ", nextNeighbor=" + nextNeighborPointer +
        '}';
    }
}
