package GML;

import java.io.*;
import java.util.*;
import Graphs.IGraph;

public class GMLReader {

    public static void readGML(String filename, IGraph<GNode> graph) throws IOException {
        Map<String, GNode> idToNode = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            // Process node blocks.
            if (line.startsWith("node")) {
                String nodeId = null;
                String nodeLabel = null;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("]")) {
                        break;
                    }
                    if (line.startsWith("id")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            nodeId = parts[1];
                        }
                    } else if (line.startsWith("label")) {
                        int firstQuote = line.indexOf("\"");
                        int lastQuote = line.lastIndexOf("\"");
                        if (firstQuote != -1 && lastQuote > firstQuote) {
                            nodeLabel = line.substring(firstQuote + 1, lastQuote);
                        }
                    }
                }
                // Use the label if present; otherwise, use the id.
                if (nodeId != null) {
                    GNode GNode = new GNode(Integer.parseInt(nodeId), nodeLabel);
                    graph.addNode(GNode);
                    idToNode.put(nodeId, GNode);
                }
            }
            // Process edge blocks.
            else if (line.startsWith("edge")) {
                String sourceId = null;
                String targetId = null;
                String edgeLabel = null; // optional
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("]")) {
                        break;
                    }
                    if (line.startsWith("source")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            sourceId = parts[1];
                        }
                    } else if (line.startsWith("target")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            targetId = parts[1];
                        }
                    } else if (line.startsWith("label")) {
                        int firstQuote = line.indexOf("\"");
                        int lastQuote = line.lastIndexOf("\"");
                        if (firstQuote != -1 && lastQuote > firstQuote) {
                            edgeLabel = line.substring(firstQuote + 1, lastQuote);
                        }
                    }
                }
                // Retrieve the corresponding GraphNode objects.
                if (sourceId != null && targetId != null) {
                    GNode sourceGNode = idToNode.get(sourceId);
                    GNode targetNode = idToNode.get(targetId);
                    // Only add the edge if both nodes were parsed.
                    if (sourceGNode != null && targetNode != null) {
                        graph.addRelationship(edgeLabel != null ? edgeLabel : "default", sourceGNode, targetNode);
                    }
                }
            }
        }
        br.close();
    }
}
