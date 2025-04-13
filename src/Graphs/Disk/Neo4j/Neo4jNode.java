package Graphs.Disk.Neo4j;

import Graphs.Disk.Constants;
import Graphs.Disk.GraphRandomAccessFile;
import Graphs.Disk.GraphRecord;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Neo4jNode implements GraphRecord {

    public static final long RECORD_SIZE = (
        Constants.BOOL_SIZE +   // In Use
        Constants.LONG_SIZE +   // First Outgoing Relationship
        Constants.LONG_SIZE     // First Incoming Relationship
    );

    private boolean inUse;
    private long outgoingPointer; // pointer for outgoing relationships
    private long incomingPointer; // pointer for incoming relationships

    // Default constructor initializes a blank (unused) node.
    public Neo4jNode() {
        this.inUse = false;
        this.outgoingPointer = -1;
        this.incomingPointer = -1;
    }

    public Neo4jNode(boolean inUse, long outgoingPointer, long incomingPointer) {
        this.inUse = inUse;
        this.outgoingPointer = outgoingPointer;
        this.incomingPointer = incomingPointer;
    }

    // Getters and setters

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public long getOutgoingPointer() {
        return outgoingPointer;
    }

    public void setOutgoingPointer(long outgoingPointer) {
        this.outgoingPointer = outgoingPointer;
    }

    public long getIncomingPointer() {
        return incomingPointer;
    }

    public void setIncomingPointer(long incomingPointer) {
        this.incomingPointer = incomingPointer;
    }

    @Override
    public int getRecordSize() {
        return (int) RECORD_SIZE;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getRecordSize());
        buffer.put((byte)(inUse ? 1 : 0));
        buffer.putLong(outgoingPointer);
        buffer.putLong(incomingPointer);
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        if (bytes.length != getRecordSize()) {
            throw new IllegalArgumentException("Invalid byte array length for Node");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        inUse = (buffer.get() != 0);
        outgoingPointer = buffer.getLong();
        incomingPointer = buffer.getLong();
    }

    @Override
    public String toString() {
        return "Node{" +
            "inUse=" + inUse +
            ", outgoingPointer=" + outgoingPointer +
            ", incomingPointer=" + incomingPointer +
        '}';
    }
}
