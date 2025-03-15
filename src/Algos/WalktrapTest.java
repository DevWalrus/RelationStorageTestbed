package Algos;

import Graphs.AdjMatrixGraph;

import java.util.Map;
import java.util.Set;

// A test class for Walktrap.
public class WalktrapTest {
    public static void main(String[] args) {
        AdjMatrixGraph<String> graph = new AdjMatrixGraph<>();

        // Create two clusters:
        // Cluster 1: A, B, C (fully connected)
        graph.addEdge("connect", "A", "B");
        graph.addEdge("connect", "B", "A");
        graph.addEdge("connect", "A", "C");
        graph.addEdge("connect", "C", "A");
        graph.addEdge("connect", "B", "C");
        graph.addEdge("connect", "C", "B");

        // Cluster 2: D, E, F (fully connected)
        graph.addEdge("connect", "D", "E");
        graph.addEdge("connect", "E", "D");
        graph.addEdge("connect", "D", "F");
        graph.addEdge("connect", "F", "D");
        graph.addEdge("connect", "E", "F");
        graph.addEdge("connect", "F", "E");

        // Add a weak link between clusters.
        graph.addEdge("connect", "C", "D");
        graph.addEdge("connect", "D", "C");

        // Create Walktrap instance with a random walk length (e.g., t = 3).
        Walktrap<String> walktrap = new Walktrap<>(graph, 3, 2);

        // Run the community detection algorithm.
        Map<String, Integer> communities = walktrap.detectCommunities();

        // Print out community assignments for manual inspection.
        System.out.println("Community assignments:");
        for (Map.Entry<String, Integer> entry : communities.entrySet()) {
            System.out.println("Node " + entry.getKey() + " -> Community " + entry.getValue());
        }

        // Optional: Write assertions (using JUnit or similar) to automatically verify that,
        // for example, nodes A, B, C belong to one community and D, E, F to another.
    }
}
