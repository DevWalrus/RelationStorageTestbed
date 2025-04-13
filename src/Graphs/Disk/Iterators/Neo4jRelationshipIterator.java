package Graphs.Disk.Iterators;

import Graphs.Disk.Constants;
import Graphs.Disk.GraphRandomAccessFile;
import Graphs.Disk.Neo4j.Neo4jDiskGraph;
import Graphs.Disk.Neo4j.Neo4jEdge;
import Graphs.Disk.Neo4j.Neo4jNode;
import Graphs.Edge;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Neo4jRelationshipIterator implements Iterator<Edge<Integer>> {
    private final int node;
    private final GraphRandomAccessFile<Neo4jNode> nodesRaf;
    private final GraphRandomAccessFile<Neo4jEdge> edgesRaf;
    private Edge<Integer> nextEdge;
    private long currentRelPos;

    public Neo4jRelationshipIterator(int node, GraphRandomAccessFile<Neo4jNode> nodesRaf, GraphRandomAccessFile<Neo4jEdge> edgesRaf) throws IOException {
        this.node = node;
        this.nodesRaf = nodesRaf;
        this.edgesRaf = edgesRaf;
        // Compute the offset in the node file:
        long nodeOffset = Constants.INT_SIZE + (node * Neo4jNode.RECORD_SIZE);
        this.nodesRaf.seek(nodeOffset);
        // Read the entire node record via our generic method.
        Neo4jNode nodeObj = this.nodesRaf.readElement(Neo4jNode::new);
        this.currentRelPos = nodeObj.getOutgoingPointer();
    }

    /**
     * Advances the file pointer until a relationship that involves the given node is found.
     * Sets nextOtherNode to the neighboring node if found.
     */
    private void advance() throws IOException {
        nextEdge = null;
        while (currentRelPos != -1 && nextEdge == null) {
            edgesRaf.seek(currentRelPos);
            // Read the whole edge record as a Neo4jEdge.
            Neo4jEdge edge = edgesRaf.readElement(Neo4jEdge::new);
            // Optional: ensure that this edge is indeed an outgoing edge for our node,
            // for extra safety (you might know that it always will be).
            if (edge.getSourceNode() == this.node) {
                nextEdge = new Edge<>(edge.getSourceNode(), edge.getTargetNode(), "default");
            } else {
                throw new RuntimeException("Why did the edge source not match the target source?");
            }
            // Advance the pointer to the next edge in the source's outgoing chain.
            currentRelPos = edge.getOutgoingNext();
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
