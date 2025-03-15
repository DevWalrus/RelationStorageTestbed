package Graphs;

public interface IGraph<T> {
    Iterable<T> getNodes();
    void addNode(T node);
    void removeNode(T node);
    void addEdge(String type, T source, T target);
    void removeEdge(String type, T source, T target);
    Iterable<T> getNeighbors(T node);
    Iterable<T> getNeighbors(T node, String type);
    T getRandomNode();
    Edge<T> getRandomEdge(T node);
    Edge<T> getRandomEdge(T node, String type);
}

