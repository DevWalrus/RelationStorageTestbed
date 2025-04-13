import Algos.Walktrap;
import Exceptions.InvalidNodeAccessException;
import GML.GNode;
import GML.GraphMLExporter;
import GML.TabImporter;
import Graphs.Edge;
import Graphs.IGraph;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class QueryBenchmark {
    private final IGraph<Integer> graph;
    private final GraphType type;
//    private static final String BASE_PATH = "C:\\Users\\clint\\OneDrive\\Documents\\Courses\\Capstone\\RelationStorageTestbed\\datasets\\";
    private static final String BASE_PATH = "C:\\Users\\Clinten\\Documents\\Courses\\2245\\Capstone\\RelationStorageTestbed\\datasets\\";
    private static final String EU_GML_LOC = BASE_PATH + "email-Eu-core.txt";
    private static final String EU_OUT_DEG_LOC = BASE_PATH + "email-Eu-core.outdeg.txt";
    private static final String EU_OUT_DEG_TIMES_LOC = BASE_PATH + "email-Eu-core.outdeg.times.txt";
//    private static final String EU_GML_LOC = BASE_PATH + "test-ds.txt";
//    private static final String EU_OUT_DEG_LOC = BASE_PATH + "test-ds.outdeg.txt";
//    private static final String EU_OUT_DEG_TIMES_LOC = BASE_PATH + "test-ds.outdeg.times.txt";
//    private static final String EU_GML_LOC = BASE_PATH + "com-dblp.ungraph.txt";
//    private static final String EU_OUT_DEG_LOC = BASE_PATH + "com-dblp.ungraph.outdeg.txt";
//    private static final String EU_OUT_DEG_TIMES_LOC = BASE_PATH + "com-dblp.ungraph.outdeg.times.txt";
    public static final int R_NODE_CNT = 10_000;
    public static final int R_EDGE_CNT = 10_000;

    public static final int R_PER_NODE = 50;

    private final List<Integer> sortedOutDegrees;

    private final HashMap<Integer, Long> sortedOutDegreesTimes = new HashMap<>();

    public QueryBenchmark(IGraph<Integer> graph, GraphType type) {
        this.graph = graph;
        this.type = type;
        sortedOutDegrees = loadSortedDegrees();
    }

    public static List<Integer> loadSortedDegrees() {
        var sortedDegrees = new ArrayList<Integer>();
        try (BufferedReader br = new BufferedReader(new FileReader(EU_OUT_DEG_LOC))) {
            String line;
            while ((line = br.readLine()) != null) {
                sortedDegrees.add(Integer.parseInt(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sortedDegrees;
    }


    interface SingleTest {
        void test() throws Exception;
    }

    private long runTimedTest(SingleTest t) throws Exception {
        long startTime = System.nanoTime();
        t.test();
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    private long runTimedTest(SingleTest t, String testName) throws Exception {
        long elapsedTime = runTimedTest(t);

        System.out.println(outputString(
                testName,
                elapsedTime));
        return elapsedTime;
    }

    private long runTimedTest(SingleTest t, String testName, int iterations) throws Exception {
        long elapsedTime = runTimedTest(t);

        System.out.println(outputString(
            testName,
            iterations,
            elapsedTime));
        return elapsedTime;
    }

    public long runBenchmark() throws Exception {
        return runBenchmark(true);
    }

    public long runBenchmark(boolean load) throws Exception {
        if (load) {
            graph.clear();
            runTimedTest(
                () -> TabImporter.readGraph(EU_GML_LOC, graph, false),
                "Import"
            );
        }

        runTimedTest(() -> {
            for (int i = 0; i < R_NODE_CNT; i++) {
                graph.getRandomNode();
            }
        }, "Random Nodes", R_NODE_CNT);

        sortedOutDegreesTimes.clear();
        for (var node : sortedOutDegrees) {
            var totalTime = runTimedTest(() -> {
                graph.getRandomRelationship(node);

//                System.out.printf("%s: ", node.toString());
//                for (Iterator<Edge<Integer>> it = graph.getRelationships(node); it.hasNext(); ) {
//                    Edge<Integer> edge_i = it.next();
//                    System.out.printf("%s, ", edge_i.getTarget().toString());
//                }
//                System.out.println();
            });
            sortedOutDegreesTimes.put(node, totalTime / R_PER_NODE);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(EU_OUT_DEG_TIMES_LOC, true))) {
            writer.println(type.name + (type.usesDisk ? "_" : ""));
            for (var node : sortedOutDegrees) {
                writer.println(sortedOutDegreesTimes.get(node));
            }
        }

//        var node = graph.getRandomNode();

//        runTimedTest(() -> {
//            for (int i = 0; i < R_EDGE_CNT; i++) {
//                graph.getRandomRelationship(node);
//            }
//        }, "Random Edges", R_EDGE_CNT);

        return 0;
    }

    private String outputString(
            String title,
            long elapsedTimeNs
    ) {
        return "\tQuery: " +
                title +
                " Took: " +
                elapsedTimeNs / (long) 1e6 +
                " ms (" +
                elapsedTimeNs / (long) 1e9 +
                "s)";
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
