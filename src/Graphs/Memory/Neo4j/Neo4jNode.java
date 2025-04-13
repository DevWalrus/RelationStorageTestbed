package Graphs.Memory.Neo4j;

public class Neo4jNode<T> {
    private final T value;
    private EdgeNode<T> firstOutgoing;
    private EdgeNode<T> firstIncoming;

    public Neo4jNode(T value) {
        this.value = value;
        this.firstOutgoing = null;
        this.firstIncoming = null;
    }

    public T getValue() {
        return value;
    }

    public EdgeNode<T> getFirstOutgoing() {
        return firstOutgoing;
    }

    public void setFirstOutgoing(EdgeNode<T> firstOutgoing) {
        this.firstOutgoing = firstOutgoing;
    }

    public EdgeNode<T> getFirstIncoming() {
        return firstIncoming;
    }

    public void setFirstIncoming(EdgeNode<T> firstIncoming) {
        this.firstIncoming = firstIncoming;
    }
}
