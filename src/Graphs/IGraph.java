package Graphs;

import Exceptions.InvalidNodeAccessException;

import java.io.IOException;

/**
 * Interface representing a generic graph.
 *
 * @param <T> the type of nodes in the graph.
 */
public interface IGraph<T> extends AutoCloseable {

    /**
     * Returns all nodes in the graph.
     *
     * @return an iterable of all nodes.
     */
    Iterable<T> getNodes();

    /**
     * Adds a node to the graph.
     *
     * @param node the node to add.
     */
    void addNode(T node) throws IOException;


    /**
     * Adds an edge of the specified label between source and target nodes.
     *
     * @param label   the label of the edge.
     * @param source the source node.
     * @param target the target node.
     */
    void addRelationship(String label, T source, T target) throws InvalidNodeAccessException, IOException;


    /**
     * Returns all neighboring nodes of the given node.
     *
     * @param node the node whose neighbors are to be returned.
     * @return an iterable of neighboring nodes.
     */
    Iterable<Edge<T>> getRelationships(T node) throws InvalidNodeAccessException, IOException;


    /**
     * Returns a random node from the graph.
     *
     * @return a random node, or null if the graph is empty.
     */
    T getRandomNode() throws IOException;

    /**
     * Returns a random edge from the specified node.
     *
     * @param node the node from which to select an edge.
     * @return a random edge, or null if there are no edges.
     */
    Edge<T> getRandomRelationship(T node) throws InvalidNodeAccessException, IOException;

    /**
     * Clear all the data from the graph.
     */
    void clear() throws IOException;

}
