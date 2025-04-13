package Graphs.Memory.Neo4j;

import Graphs.Edge;

public class EdgeNode<T> {
    public Edge<T> edge;

    // Pointers for the outgoing edge list (for the source node)
    public EdgeNode<T> outNext;
    public EdgeNode<T> outPrev;

    // Pointers for the incoming edge list (for the target node)
    public EdgeNode<T> inNext;
    public EdgeNode<T> inPrev;

    public EdgeNode(Edge<T> edge) {
        this.edge = edge;
        outNext = null;
        outPrev = null;
        inNext = null;
        inPrev = null;
    }

}
