package Graphs.Neo4j;

import Graphs.Edge;

public class EdgeNode<T> {
    public Edge<T> edge;
    public EdgeNode<T> next;

    public EdgeNode(Edge<T> edge) {
        this.edge = edge;
        this.next = null;
    }
}
