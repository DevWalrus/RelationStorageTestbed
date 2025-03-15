package Graphs;

public class Edge<T> {
    private final T source;
    private final T target;
    // Optional properties like weight, label, etc.

    public Edge(T source, T target) {
        this.source = source;
        this.target = target;
    }

    public T getSource() {
        return source;
    }

    public T getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Edge{ " +
                "source=" + source.toString() +
                ", target=" + target.toString() +
                " }";
    }
}