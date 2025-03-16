import Graphs.*;
import Graphs.Neo4j.Neo4jGraph;

public class GraphFactory {
    public static <T> IGraph<T> createGraph(GraphType type) {
        switch (type) {
            case NEO4J:
                return new Neo4jGraph<>();
            case ADJ_MATRIX:
                return new AdjMatrixGraph<>();
            case ADJ_CNT_MATRIX:
                return new AdjMatrixCountGraph<>();
            case EDGE_LIST:
                return new EdgeListGraph<>();
            case SIMPLE:
                return new SimpleGraph<>();
            default:
                throw new IllegalArgumentException("Unknown graph type: " + type);
        }
    }
}
