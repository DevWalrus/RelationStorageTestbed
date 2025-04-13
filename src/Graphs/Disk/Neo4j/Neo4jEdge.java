package Graphs.Disk.Neo4j;

import Graphs.Disk.Constants;
import Graphs.Disk.GraphRecord;

import java.nio.ByteBuffer;

public class Neo4jEdge implements GraphRecord {
    public static final long RECORD_SIZE = (
        Constants.INT_SIZE +    // Start Node
        Constants.INT_SIZE +    // End Node
        Constants.LONG_SIZE +   // The next outgoing relationship
        Constants.LONG_SIZE +   // The previous outgoing relationship
        Constants.LONG_SIZE +   // The next incoming relationship
        Constants.LONG_SIZE +   // The previous incoming relationship
        Constants.BOOL_SIZE     // End of chain flag
    );

    private int sourceNode;
    private int targetNode;
    private long outgoingNext;   // source's outgoing chain "next" pointer
    private long outgoingPrev;   // source's outgoing chain "previous" pointer
    private long incomingNext;   // target's incoming chain "next" pointer
    private long incomingPrev;   // target's incoming chain "previous" pointer
    private boolean chainFlag;   // optional chain flag

    // Constructors
    public Neo4jEdge() {
        this.sourceNode = 0;
        this.targetNode = 0;
        this.outgoingNext = -1;
        this.outgoingPrev = -1;
        this.incomingNext = -1;
        this.incomingPrev = -1;
        this.chainFlag = false;
    }

    public Neo4jEdge(int sourceNode, int targetNode, long outgoingNext, long outgoingPrev,
                long incomingNext, long incomingPrev, boolean chainFlag) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.outgoingNext = outgoingNext;
        this.outgoingPrev = outgoingPrev;
        this.incomingNext = incomingNext;
        this.incomingPrev = incomingPrev;
        this.chainFlag = chainFlag;
    }

    // Getters and setters
    public int getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(int sourceNode) {
        this.sourceNode = sourceNode;
    }

    public int getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(int targetNode) {
        this.targetNode = targetNode;
    }

    public long getOutgoingNext() {
        return outgoingNext;
    }

    public void setOutgoingNext(long outgoingNext) {
        this.outgoingNext = outgoingNext;
    }

    public long getOutgoingPrev() {
        return outgoingPrev;
    }

    public void setOutgoingPrev(long outgoingPrev) {
        this.outgoingPrev = outgoingPrev;
    }

    public long getIncomingNext() {
        return incomingNext;
    }

    public void setIncomingNext(long incomingNext) {
        this.incomingNext = incomingNext;
    }

    public long getIncomingPrev() {
        return incomingPrev;
    }

    public void setIncomingPrev(long incomingPrev) {
        this.incomingPrev = incomingPrev;
    }

    public boolean isChainFlag() {
        return chainFlag;
    }

    public void setChainFlag(boolean chainFlag) {
        this.chainFlag = chainFlag;
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
        buffer.putInt(sourceNode);
        buffer.putInt(targetNode);
        buffer.putLong(outgoingNext);
        buffer.putLong(outgoingPrev);
        buffer.putLong(incomingNext);
        buffer.putLong(incomingPrev);
        buffer.put((byte)(chainFlag ? 1 : 0));
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
        sourceNode = buffer.getInt();
        targetNode = buffer.getInt();
        outgoingNext = buffer.getLong();
        outgoingPrev = buffer.getLong();
        incomingNext = buffer.getLong();
        incomingPrev = buffer.getLong();
        chainFlag = (buffer.get() != 0);
    }

    @Override
    public String toString() {
        return "Edge{" +
            "sourceNode=" + sourceNode +
            ", targetNode=" + targetNode +
            ", outgoingNext=" + outgoingNext +
            ", outgoingPrev=" + outgoingPrev +
            ", incomingNext=" + incomingNext +
            ", incomingPrev=" + incomingPrev +
            ", chainFlag=" + chainFlag +
        '}';
    }
}
