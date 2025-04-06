import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    private static final int RUN_CNT = 10;
    private static final String DIR_PATH = "C:\\Benchmark\\";
    private static final String NODES_NAME = "nodes.dat";
    private static final String EDGES_NAME = "edges.dat";


    public static void main(String[] args) throws IOException {
        for (GraphType ty : List.of(
                GraphType.NEO4J,
                GraphType.ADJ_MATRIX,
                GraphType.SIMPLE,
                GraphType.EDGE_LIST,
                GraphType.LINKED_LIST_DISK,
                GraphType.EDGE_LIST_DISK
        )) {
            for (int i = 0; i < RUN_CNT; i++) {
                System.out.println(ty.name() + " (" + i + "):");
                runDiskBenchmark(ty, DIR_PATH, NODES_NAME, EDGES_NAME);
            }
        }
    }

    public static long runDiskBenchmark(
        GraphType type,
        String directoryPath,
        String nodesFileName,
        String edgesFileName
    ) {
        var nodeOutputName = type.name() + "_" + nodesFileName;
        var edgeOutputName = type.name() + "_" + edgesFileName;
        var nodeFile = new File(directoryPath + nodeOutputName);
        var edgeFile = new File(directoryPath + edgeOutputName);

        try (var graph = GraphFactory.createGraph(type, directoryPath, nodeOutputName, edgeOutputName)) {
            QueryBenchmark benchmark = new QueryBenchmark(graph, type);
            return benchmark.runBenchmark(!type.usesDisk);
        } catch (Exception e) {
            System.out.flush();
            System.out.println("\tTest failed due to: " + e.getMessage());
        }
//        if (!nodeFile.delete()) System.out.println("\tFailed to delete " + nodeFile.getAbsolutePath());
//        if (!edgeFile.delete()) System.out.println("\tFailed to delete " + edgeFile.getAbsolutePath());
        return 0;
    }
}
