import Algos.Walktrap;
import GML.GMLReader;
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

public class GraphBenchmark {
    private final IGraph<GNode> graph;
    private final GraphType type;
    private static final String GML_LOC = "C:\\Users\\Clinten\\Documents\\Courses\\2245\\Capstone\\RelationStorageTestbed\\datasets\\com-youtube.ungraph.txt";

    public GraphBenchmark(IGraph<GNode> graph, GraphType type) {
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

    public void runBenchmark() throws IOException {
        long elapsedTimeNs = runTimedTest(() -> TabImporter.readGraph(GML_LOC, graph));

        System.out.println(outputString(
                "Read Data",
                elapsedTimeNs));

        Walktrap<GNode> walktrap = new Walktrap<>(graph, 10, true, false);
        AtomicReference<Walktrap<GNode>.WalktrapResult> result = new AtomicReference<>();

        elapsedTimeNs = runTimedTest(() -> result.set(walktrap.run()));

        System.out.println(outputString(
                "Run Walktrap",
                elapsedTimeNs));

        List<Double> mods = result.get().modularities;
        int bestIndex = 0;
        double bestMod = mods.getFirst();
        for (int i = 1; i < mods.size(); i++) {
            if (mods.get(i) > bestMod) {
                bestMod = mods.get(i);
                bestIndex = i;
            }
        }
        Set<Integer> bestPartition = result.get().partitions.get(bestIndex);

        Map<Integer, Integer> bestAssignment = new HashMap<>();
        for (Integer commId : bestPartition) {
            Set<Integer> vertices = result.get().communities.get(commId).vertices;
            for (Integer v : vertices) {
                bestAssignment.put(v, commId);
            }
        }

        try (PrintWriter pw = new PrintWriter("best_partition_" + type.name() + ".csv")) {
            pw.println("node,community");
            for (Map.Entry<Integer, Integer> entry : bestAssignment.entrySet()) {
                pw.println(entry.getKey() + "," + entry.getValue());
            }
        }

        GraphMLExporter.exportToGraphML(graph, bestAssignment.entrySet(), "football_export_" + type.name() + ".graphml");
    }

    private String outputString(
        String title,
        long elapsedTimeNs
    ) {
        return "\t" +
                title +
                " - " +
                elapsedTimeNs / (long) 1e6 +
                " ms (" +
                elapsedTimeNs / (long) 1e9 +
                "s)";
    }
}
