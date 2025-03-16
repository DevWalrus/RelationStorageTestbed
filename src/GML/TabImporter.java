package GML;

import Graphs.IGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TabImporter {

    public static void readGraph(String filename, IGraph<GNode> graph) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            // Skip header or empty lines.
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            // Split the line on whitespace (tabs or spaces)
            String[] tokens = line.split("\\s+");
            if (tokens.length < 2) {
                continue; // ignore malformed lines
            }
            try {
                int fromId = Integer.parseInt(tokens[0]);
                GNode from = new GNode(fromId, String.valueOf(fromId));
                int toId = Integer.parseInt(tokens[0]);
                GNode to = new GNode(toId, String.valueOf(toId));
                graph.addEdge("default", from, to);
                graph.addEdge("default", to, from);
            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid line: " + line);
            }
        }
        br.close();
    }
}
