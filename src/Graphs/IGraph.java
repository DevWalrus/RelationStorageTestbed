package Graphs;

/**
 * Interface representing a generic graph.
 *
 * @param <T> the type of nodes in the graph.
 */
public interface IGraph<T> {

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
    void addNode(T node);

    /**
     * Removes a node from the graph.
     *
     * @param node the node to remove.
     */
    void removeNode(T node);

    /**
     * Adds an edge of the specified label between source and target nodes.
     *
     * @param label   the label of the edge.
     * @param source the source node.
     * @param target the target node.
     */
    void addRelationship(String label, T source, T target);

    /**
     * Removes an edge of the specified label between source and target nodes.
     *
     * @param label   the label of the edge.
     * @param source the source node.
     * @param target the target node.
     */
    void removeRelationship(String label, T source, T target);

    /**
     * Returns all neighboring nodes of the given node.
     *
     * @param node the node whose neighbors are to be returned.
     * @return an iterable of neighboring nodes.
     */
    Iterable<T> getRelationships(T node);

    /**
     * Returns neighboring nodes of the given node filtered by edge label.
     *
     * @param node the node whose neighbors are to be returned.
     * @param label the edge label to filter neighbors.
     * @return an iterable of neighboring nodes connected via edges of the specified label.
     */
    Iterable<T> getRelationships(T node, String label);

    /**
     * Returns a random node from the graph.
     *
     * @return a random node, or null if the graph is empty.
     */
    T getRandomNode();

    /**
     * Returns a random edge from the specified node.
     *
     * @param node the node from which to select an edge.
     * @return a random edge, or null if there are no edges.
     */
    Edge<T> getRandomRelationship(T node);

    /**
     * Returns a random edge of the specified label from the given node.
     *
     * @param node the node from which to select an edge.
     * @param label the label of the edge to select.
     * @return a random edge of the specified label, or null if none exists.
     */
    Edge<T> getRandomRelationship(T node, String label);
}
