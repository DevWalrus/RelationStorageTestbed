import Graphs.IGraph;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GraphBenchmark {
    private final IGraph<Integer> graph;
    private static final Integer NODE_COUNT = 1_000;
    private static final Integer EDGE_COUNT = 50_000_000;
    private static final Integer R_LOOKUP_COUNT = 100_000;
    private static final Integer R_EDGE_LOOKUP_COUNT = 100_000;
    private static final List<String> RELATIONSHIPS = List.of("FRIEND", "MOTHER", "FATHER", "TEACHER", "PARTNER");
    private static final int RELATIONSHIPS_LEN = 5;

    public GraphBenchmark(IGraph<Integer> graph) {
        this.graph = graph;
    }

    interface SingleTest {
        void test();
    }

    private long runTimedTest(SingleTest t) {
        long startTime = System.nanoTime();
        t.test();
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    public void runBenchmark() {
        long elapsedTimeNs = runTimedTest(() -> {
            for (int i = 0; i < NODE_COUNT; i++) {
                graph.addNode(i);
            }
        });

        System.out.println(outputString(
                "Node creation",
                NODE_COUNT,
                elapsedTimeNs));

        elapsedTimeNs = runTimedTest(() -> {
            for (int i = 0; i < EDGE_COUNT; i++) {
                int source = (int)(Math.random() * 1000);
                int target = (int)(Math.random() * 1000);
                graph.addEdge(RELATIONSHIPS.get(ThreadLocalRandom.current().nextInt(RELATIONSHIPS_LEN)), source, target);
            }
        });

        System.out.println(outputString(
                "Edge creation",
                EDGE_COUNT,
                elapsedTimeNs));

        elapsedTimeNs = runTimedTest(() -> {
            for (int i = 0; i < R_LOOKUP_COUNT; i++) {
                graph.getRandomNode();
            }
        });

        System.out.println(outputString(
                "Random Node",
                R_LOOKUP_COUNT,
                elapsedTimeNs));

        Integer node = graph.getRandomNode();
        elapsedTimeNs = runTimedTest(() -> {
            for (int i = 0; i < R_EDGE_LOOKUP_COUNT; i++) {
                graph.getRandomEdge(node);
            }
        });

        System.out.println(outputString(
                "Random Edge",
                R_EDGE_LOOKUP_COUNT,
                elapsedTimeNs));

        Integer node2 = graph.getRandomNode();
        String type = RELATIONSHIPS.getFirst();
        elapsedTimeNs = runTimedTest(() -> {
            for (int i = 0; i < R_EDGE_LOOKUP_COUNT; i++) {
                graph.getRandomEdge(node2, type);
            }
        });

        System.out.println(outputString(
                "Random \""+ RELATIONSHIPS.getFirst() + "\" Edge",
                R_EDGE_LOOKUP_COUNT,
                elapsedTimeNs));
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
