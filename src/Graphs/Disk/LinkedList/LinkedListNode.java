package Graphs.Disk.LinkedList;

import Graphs.Disk.Constants;
import Graphs.Disk.GraphRecord;

import java.nio.ByteBuffer;

public class LinkedListNode implements GraphRecord {

    public static final long RECORD_SIZE = (
        Constants.BOOL_SIZE +   // In Use
        Constants.INT_SIZE +    // Node Id
        Constants.LONG_SIZE     // Neighbor Pointer
    );

    private boolean inUse;
    private int nodeId; // pointer for outgoing relationships
    private long neighborPointer; // pointer for incoming relationships

    // Default constructor initializes a blank (unused) node.
    public LinkedListNode() {
        this(false, -1, -1);
    }

    public LinkedListNode(boolean inUse, int nodeId, long neighborPointer) {
        this.inUse = inUse;
        this.nodeId = nodeId;
        this.neighborPointer = neighborPointer;
    }

    // Getters and setters

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public long getNeighborPointer() {
        return neighborPointer;
    }

    public void setNeighborPointer(long neighborPointer) {
        this.neighborPointer = neighborPointer;
    }

    @Override
    public int getRecordSize() {
        return (int) RECORD_SIZE;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getRecordSize());
        buffer.put((byte)(inUse ? 1 : 0));
        buffer.putInt(nodeId);
        buffer.putLong(neighborPointer);
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        if (bytes.length != getRecordSize()) {
            throw new IllegalArgumentException("Invalid byte array length for Node");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        inUse = (buffer.get() != 0);
        nodeId = buffer.getInt();
        neighborPointer = buffer.getLong();
    }

    @Override
    public String toString() {
        return "Node{" +
            "inUse=" + inUse +
            ", nodeId=" + nodeId +
            ", neighborPointer=" + neighborPointer +
        '}';
    }
}
