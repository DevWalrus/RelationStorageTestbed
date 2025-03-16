package Graphs;

public class Edge<T> {
    private final T source;
    private final T target;
    private final String label;

    public Edge(T source, T target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public T getSource() {
        return source;
    }

    public T getTarget() {
        return target;
    }

    public String getLabel() {return label;}

    @Override
    public String toString() {
        return "Edge{ " +
                "source=" + source.toString() +
                ", target=" + target.toString() +
                ", label=" + label +
                " }";
    }
}