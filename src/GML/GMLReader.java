package GML;

import java.io.*;
import java.util.*;

import Exceptions.InvalidNodeAccessException;
import Graphs.IGraph;

public class GMLReader {

    public static void readGML(String filename, IGraph<Integer> graph) throws IOException, InvalidNodeAccessException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("node")) {
                String nodeId = null;
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
                    }
                }
                assert nodeId != null;
                graph.addNode(Integer.parseInt(nodeId));
            }
            // Process edge blocks.
            else if (line.startsWith("edge")) {
                Integer sourceId = null;
                Integer targetId = null;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("]")) {
                        break;
                    }
                    if (line.startsWith("source")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            sourceId = Integer.parseInt(parts[1]);
                        }
                    } else if (line.startsWith("target")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            targetId = Integer.parseInt(parts[1]);
                        }
                    }
                }
                graph.addRelationship("default", sourceId, targetId);
            }
        }
        br.close();
    }
}
