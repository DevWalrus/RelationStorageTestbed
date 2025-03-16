import GML.GNode;
import Graphs.IGraph;

import java.util.List;

public class Main {
    private static final int RUN_CNT = 1;

    public static void main(String[] args) {
        for (GraphType ty : List.of(
                GraphType.NEO4J,
                GraphType.ADJ_MATRIX,
                GraphType.ADJ_CNT_MATRIX,
                GraphType.EDGE_LIST,
                GraphType.SIMPLE
        )) {
            long avg = 0;
            for (int i = 0; i < RUN_CNT; i++) {
//                System.out.println(ty.name() + " (" + i + "):");
                avg += runBenchmark(ty);
            }
            System.out.println(ty.name() + ":\n\tAverage Walktrap - " + avg / RUN_CNT + "s");
        }
    }

    public static long runBenchmark(GraphType type) {
        IGraph<GNode> graph = GraphFactory.createGraph(type);
        GraphBenchmark benchmark = new GraphBenchmark(graph, type);
        try {
            return benchmark.runBenchmark();
        } catch (Exception e) {
            System.out.flush();
            System.out.println("\tTest failed due to: " + e.getMessage());
        }
        return 0;
    }
}
