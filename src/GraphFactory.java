import Graphs.*;
import Graphs.Disk.EdgeListDiskGraph;
import Graphs.Disk.LinkedListDiskGraph;
import Graphs.Memory.AdjMatrixCountGraph;
import Graphs.Memory.AdjMatrixGraph;
import Graphs.Memory.EdgeListGraph;
import Graphs.Memory.SimpleGraph;
import Graphs.Memory.Neo4j.Neo4jGraph;

import java.io.IOException;

public class GraphFactory {
    public static IGraph<Integer> createGraph(GraphType type) throws IOException {
        return switch (type) {
            case NEO4J -> new Neo4jGraph<>();
            case ADJ_MATRIX -> new AdjMatrixGraph<>();
            case ADJ_CNT_MATRIX -> new AdjMatrixCountGraph<>();
            case EDGE_LIST -> new EdgeListGraph<>();
            case SIMPLE -> new SimpleGraph<>();
            case LINKED_LIST_DISK -> new LinkedListDiskGraph("C:\\");
            case EDGE_LIST_DISK -> new EdgeListDiskGraph("C:\\");
            default -> throw new IllegalArgumentException("Unsupported graph type: " + type);
        };
    }

    public static IGraph<Integer> createGraph(GraphType type, String directoryPath, String nodeOutputName, String edgeOutputName) throws IOException {
        return switch (type) {
            case NEO4J -> new Neo4jGraph<>();
            case ADJ_MATRIX -> new AdjMatrixGraph<>();
            case ADJ_CNT_MATRIX -> new AdjMatrixCountGraph<>();
            case EDGE_LIST -> new EdgeListGraph<>();
            case SIMPLE -> new SimpleGraph<>();
            case LINKED_LIST_DISK -> new LinkedListDiskGraph(directoryPath, nodeOutputName, edgeOutputName);
            case EDGE_LIST_DISK -> new EdgeListDiskGraph(directoryPath, nodeOutputName, edgeOutputName);
            default -> throw new IllegalArgumentException("Unsupported graph type: " + type);
        };
    }
}
