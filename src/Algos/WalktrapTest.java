package Algos;

import Exceptions.InvalidNodeAccessException;
import GML.GMLReader;
import GML.GraphMLExporter;
import Graphs.IGraph;
import Graphs.Memory.SimpleGraph;

import java.util.*;
import java.io.*;

public class WalktrapTest {
    public static void main(String[] args) throws IOException, InvalidNodeAccessException {

        IGraph<Integer> graph = new SimpleGraph<>();
        GMLReader.readGML(
            "C:\\Users\\Clinten\\Documents\\Courses\\2245\\Capstone\\RelationStorageTestbed\\datasets\\football.gml",
            graph
        );

        var walktrap = new Walktrap<>(graph, 10, true, false);

        long startTime = System.nanoTime();
        var result = walktrap.run();
        long endTime = System.nanoTime();
        System.out.println(endTime - startTime);

        List<Double> mods = result.modularities;
        int bestIndex = 0;
        double bestMod = mods.getFirst();
        for (int i = 1; i < mods.size(); i++) {
            if (mods.get(i) > bestMod) {
                bestMod = mods.get(i);
                bestIndex = i;
            }
        }

        Set<Integer> bestPartition = result.partitions.get(bestIndex);

        Map<Integer, Integer> bestAssignment = new HashMap<>();
        for (Integer commId : bestPartition) {
            Set<Integer> vertices = result.communities.get(commId).vertices;
            for (Integer v : vertices) {
                bestAssignment.put(v, commId);
            }
        }

        try (PrintWriter pw = new PrintWriter("best_partition.csv")) {
            pw.println("node,community");
            for (Map.Entry<Integer, Integer> entry : bestAssignment.entrySet()) {
                pw.println(entry.getKey() + "," + entry.getValue());
            }
        }

        System.out.println("Best partition mapping (node -> community):");
        for (Map.Entry<Integer, Integer> entry : bestAssignment.entrySet()) {
            System.out.println("Node " + entry.getKey() + " -> Community " + entry.getValue());
        }

        GraphMLExporter.exportToGraphML(graph, bestAssignment.entrySet(), "football_export.graphml");
        System.out.println("Graph exported to 'football_export.graphml'.");
    }
}
