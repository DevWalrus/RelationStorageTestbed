package GML;

import Graphs.IGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

public class GraphMLExporter {

    /**
     * Exports an IGraph into GraphML with a community attribute.
     * bestAssignment maps a node's integer id to its community id.
     *
     * @param bestAssignments The mapping from node id to community id.
     * @param filename       The output filename.
     * @throws IOException   If an I/O error occurs.
     */
    public static void exportToGraphML(IGraph<GNode> graph, Set<Map.Entry<Integer, Integer>> bestAssignments, String filename) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(filename));

        // Write GraphML header.
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"");
        pw.println("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        pw.println("         xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns");
        pw.println("         http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");

        // Define key for community attribute.
        pw.println("  <key id=\"d0\" for=\"node\" attr.name=\"community\" attr.type=\"int\"/>");

        // Start graph element.
        pw.println("  <graph id=\"G\" edgedefault=\"undirected\">");

        // Write nodes.
        // We assume that each node's identifier can be obtained from node.toString()
        // and that it represents an integer.
        for (Map.Entry<Integer, Integer> entry : bestAssignments) {
            // Obtain node id as integer.
            int nodeId;
            try {
                nodeId = Integer.parseInt(entry.getKey().toString());
            } catch(NumberFormatException e) {
                nodeId = entry.getKey().hashCode();
            }
            // Look up community from bestAssignment.
            int community = entry.getValue();
            pw.println("    <node id=\"" + nodeId + "\">");
            pw.println("      <data key=\"d0\">" + community + "</data>");
            pw.println("    </node>");
        }

        // Write edges.
        // Since IGraph does not provide a method for all edges, iterate over nodes and their neighbors.
        Set<String> seenEdges = new HashSet<>();
        for (GNode source : graph.getNodes()) {
            int sourceId = source.getId();
            for (GNode target : graph.getRelationships(source)) {
                int targetId = target.getId();
                String edgeKey = sourceId <= targetId ? sourceId + "_" + targetId : targetId + "_" + sourceId;
                if (seenEdges.contains(edgeKey)) continue;
                seenEdges.add(edgeKey);
                pw.println("    <edge id=\"" + edgeKey + "\" source=\"" + sourceId + "\" target=\"" + targetId + "\"/>");
            }
        }

        pw.println("  </graph>");
        pw.println("</graphml>");
        pw.close();
    }
}
