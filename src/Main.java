import Graphs.IGraph;

public class Main {
    public static void main(String[] args) {
        System.out.println("Neo4j:");
        runBenchmark(GraphType.NEO4J);
        System.out.println("Adj Matrix:");
        runBenchmark(GraphType.ADJ_MATRIX);
    }

    public static void runBenchmark(GraphType type) {
        IGraph<Integer> graph = GraphFactory.createGraph(type);
        GraphBenchmark benchmark = new GraphBenchmark(graph);
        try {
            benchmark.runBenchmark();
        } catch (Exception e) {
            System.out.flush();
            System.out.println("\tTest failed due to: " + e.getMessage());
        }

    }
}
