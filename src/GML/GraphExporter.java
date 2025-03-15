package GML;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import Graphs.IGraph;

/**
 * Exports an IGraph into GraphML format for use in Gephi.
 * This exporter assumes that the IGraph implementation provides a method getNodes()
 * that returns all nodes in the graph.
 */
public class GraphExporter {

    /**
     * Exports the given graph to a GraphML file.
     *
     * @param graph    The graph to export.
     * @param filename The output filename.
     * @param <T>      The type of nodes.
     * @throws IOException If an I/O error occurs.
     */
    public static <T> void exportToGraphML(IGraph<T> graph, String filename) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(filename));

        // Write GraphML header.
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"");
        pw.println("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        pw.println("         xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns");
        pw.println("         http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
        pw.println("  <graph id=\"G\" edgedefault=\"undirected\">");

        // Write nodes.
        // Assumes that graph.getNodes() returns an Iterable<T> of all nodes.
        for (T node : graph.getNodes()) {
            // Use node.toString() as the node id (modify if needed).
            String nodeId = node.toString();
            pw.println("    <node id=\"" + nodeId + "\">");
            // Optionally include a label.
            pw.println("      <data key=\"label\">" + nodeId + "</data>");
            pw.println("    </node>");
        }

        // Write edges.
        // Since IGraph doesn't have a method to get all edges,
        // we iterate over each node and its neighbors.
        // For undirected graphs, we output each edge only once.
        Set<String> seenEdges = new HashSet<>();
        for (T source : graph.getNodes()) {
            for (T target : graph.getNeighbors(source)) {
                // Create a canonical edge key (order the two node IDs).
                String s = source.toString();
                String t = target.toString();
                String edgeKey = s.compareTo(t) <= 0 ? s + "_" + t : t + "_" + s;
                if (seenEdges.contains(edgeKey)) {
                    continue;
                }
                seenEdges.add(edgeKey);
                pw.println("    <edge id=\"" + edgeKey + "\" source=\"" + s + "\" target=\"" + t + "\"/>");
            }
        }

        pw.println("  </graph>");
        pw.println("</graphml>");
        pw.close();
    }
}
