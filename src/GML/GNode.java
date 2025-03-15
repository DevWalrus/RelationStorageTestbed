package GML;

import java.util.Objects;

public class GNode {
    private final int id;
    private final String label;

    public GNode(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GNode GNode = (GNode) o;
        return Objects.equals(id, GNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{ " +
                "id='" + id +
                "', label='" + label +
                "' }";
    }
}
