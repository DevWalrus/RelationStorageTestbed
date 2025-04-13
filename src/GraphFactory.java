import Graphs.*;
import Graphs.Disk.EdgeListDiskGraph;
import Graphs.Disk.LinkedList.LinkedListDiskGraph;
import Graphs.Disk.Neo4j.Neo4jDiskGraph;
import Graphs.Memory.AdjMatrixGraph;
import Graphs.Memory.EdgeListGraph;
import Graphs.Memory.AdjListGraph;
import Graphs.Memory.Neo4j.Neo4jGraph;

import java.io.IOException;

public class GraphFactory {
    public static IGraph<Integer> createGraph(GraphType type) throws IOException {
        return switch (type) {
            case NEO4J -> new Neo4jGraph<>();
            case ADJ_MATRIX -> new AdjMatrixGraph<>();
            case EDGE_LIST -> new EdgeListGraph<>();
            case SIMPLE -> new AdjListGraph<>();
            case LINKED_LIST_DISK -> new LinkedListDiskGraph("C:\\");
            case EDGE_LIST_DISK -> new EdgeListDiskGraph("C:\\");
            case NEO4J_DISK -> new Neo4jDiskGraph("C:\\");
            default -> throw new IllegalArgumentException("Unsupported graph type: " + type);
        };
    }

    public static IGraph<Integer> createGraph(GraphType type, String directoryPath, String nodeOutputName, String edgeOutputName) throws IOException {
        return switch (type) {
            case NEO4J -> new Neo4jGraph<>();
            case ADJ_MATRIX -> new AdjMatrixGraph<>();
            case EDGE_LIST -> new EdgeListGraph<>();
            case SIMPLE -> new AdjListGraph<>();
            case LINKED_LIST_DISK -> new LinkedListDiskGraph(directoryPath, nodeOutputName, edgeOutputName);
            case EDGE_LIST_DISK -> new EdgeListDiskGraph(directoryPath, nodeOutputName, edgeOutputName);
            case NEO4J_DISK -> new Neo4jDiskGraph(directoryPath, nodeOutputName, edgeOutputName);
            default -> throw new IllegalArgumentException("Unsupported graph type: " + type);
        };
    }
}
