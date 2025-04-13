import Exceptions.InvalidNodeAccessException;
import GML.TabImporter;
import Graphs.Edge;
import Graphs.IGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GetOutDegree {
    private static final String BASE_PATH = "C:\\Users\\Clinten\\Documents\\Courses\\2245\\Capstone\\RelationStorageTestbed\\datasets\\";
//    private static final String EU_GML_LOC = BASE_PATH + "email-Eu-core.txt";
//    private static final String EU_OUT_DEG_LOC = BASE_PATH + "email-Eu-core.outdeg.txt";
//    private static final String EU_OUT_DEG_FULL_LOC = BASE_PATH + "email-Eu-core.outdeg.full.txt";
//    private static final String EU_GML_LOC = BASE_PATH + "test-ds.txt";
//    private static final String EU_OUT_DEG_LOC = BASE_PATH + "test-ds.outdeg.txt";
//    private static final String EU_OUT_DEG_FULL_LOC = BASE_PATH + "test-ds.outdeg.full.txt";
    private static final String EU_GML_LOC = BASE_PATH + "com-dblp.ungraph.txt";
    private static final String EU_OUT_DEG_LOC = BASE_PATH + "com-dblp.ungraph.outdeg.txt";
    private static final String EU_OUT_DEG_FULL_LOC = BASE_PATH + "com-dblp.ungraph.outdeg.full.txt";

    public static void main(String[] args) throws IOException, InvalidNodeAccessException {
        IGraph<Integer> graph = GraphFactory.createGraph(GraphType.SIMPLE);
        TabImporter.readGraph(EU_GML_LOC, graph, true);
        var outDegrees = new HashMap<Integer, Integer>();
        for (Iterator<Integer> it = graph.getNodes(); it.hasNext(); ) {
            var node = it.next();
            var r_cnt = 0;
            for (Iterator<Edge<Integer>> iter = graph.getRelationships(node); iter.hasNext(); ) {
                Edge<Integer> _ = iter.next();
                r_cnt++;
            }
            outDegrees.put(node, r_cnt);
        }
        for (var val : outDegrees.entrySet()) {
            System.out.println(val.getKey() + ": " + val.getValue());
        }

        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(outDegrees.entrySet());
        sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        try (PrintWriter writer = new PrintWriter(new FileWriter(EU_OUT_DEG_LOC))) {
            for (Map.Entry<Integer, Integer> entry : sortedEntries) {
                writer.println(entry.getKey());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(EU_OUT_DEG_FULL_LOC))) {
            for (Map.Entry<Integer, Integer> entry : sortedEntries) {
                writer.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
