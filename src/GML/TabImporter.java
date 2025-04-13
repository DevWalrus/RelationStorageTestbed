package GML;

import Exceptions.InvalidNodeAccessException;
import Graphs.IGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TabImporter {

    public static void readGraph(String filename, IGraph<Integer> graph, boolean directed) throws IOException {
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
                System.err.println("Skipping invalid line: " + line);
                continue; // ignore malformed lines
            }
            try {
                int fromId = Integer.parseInt(tokens[0]);
                int toId = Integer.parseInt(tokens[1]);
                graph.addNode(fromId);
                graph.addNode(toId);
                graph.addRelationship("default", fromId, toId);
                if (!directed) graph.addRelationship("default", toId, fromId);
            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid line: " + line);
            } catch (InvalidNodeAccessException e) {
                throw new RuntimeException(e);
            }
        }
        br.close();
    }
}
