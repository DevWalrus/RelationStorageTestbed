import Algos.Walktrap;
import GML.GNode;
import GML.GraphMLExporter;
import GML.TabImporter;
import Graphs.IGraph;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class QueryBenchmark {
    private final IGraph<GNode> graph;
    private final GraphType type;
//    private static final String BASE_PATH = "C:\\Users\\clint\\OneDrive\\Documents\\Courses\\Capstone\\RelationStorageTestbed\\datasets\\";
    private static final String BASE_PATH = "C:\\Users\\Clinten\\Documents\\Courses\\2245\\Capstone\\RelationStorageTestbed\\datasets\\";
    private static final String EU_GML_LOC = BASE_PATH + "email-Eu-core.txt";

    public static final int R_NODE_CNT = 10_000_000;
    public static final int R_EDGE_CNT = 10_000_000;

    public QueryBenchmark(IGraph<GNode> graph, GraphType type) {
        this.graph = graph;
        this.type = type;
    }

    interface SingleTest {
        void test() throws IOException;
    }

    private long runTimedTest(SingleTest t) throws IOException {
        long startTime = System.nanoTime();
        t.test();
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    public long runBenchmark() throws IOException {
        TabImporter.readGraph(EU_GML_LOC, graph, false);

        long elapsedTimeNs = runTimedTest(() -> {
            for (int i = 0; i < R_NODE_CNT; i++) {
                graph.getRandomNode();
            }
        });

        System.out.println(outputString(
                "Random Nodes",
                R_NODE_CNT,
                elapsedTimeNs));

        GNode node = graph.getRandomNode();

        elapsedTimeNs = runTimedTest(() -> {
            for (int i = 0; i < R_EDGE_CNT; i++) {
                graph.getRandomRelationship(node);
            }
        });

        System.out.println(outputString(
                "Random Edges",
                R_EDGE_CNT,
                elapsedTimeNs));

        return 0;
    }

    private String outputString(
            String title,
            int iterations,
            long elapsedTimeNs
    ) {
        return "\tQuery: " +
                title +
                " (" +
                iterations +
                ") Took: " +
                elapsedTimeNs / (long) 1e6 +
                " ms (" +
                elapsedTimeNs / (long) 1e9 +
                "s)";
    }
}
