import Graphs.*;

public class GraphFactory {
    public static <T> IGraph<T> createGraph(GraphType type) {
        switch (type) {
            case NEO4J:
                return new Neo4jGraph<>();
            case ADJ_MATRIX:
                return new AdjMatrixGraph<>();
            default:
                throw new IllegalArgumentException("Unknown graph type: " + type);
        }
    }
}
