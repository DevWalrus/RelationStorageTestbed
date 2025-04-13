public enum GraphType {
    NEO4J(false, "Neo4j"),
    ADJ_MATRIX(false, "Adjacency Matrix"),
    EDGE_LIST(false, "Edge List"),
    SIMPLE(false, "Adjacency List"),
    EDGE_LIST_DISK(true, "Edge List"),
    LINKED_LIST_DISK(true, "Adjacency List"),
    NEO4J_DISK(true, "Neo4j");

    public final boolean usesDisk;
    public final String name;

    GraphType(boolean usesDisk, String name) {
        this.usesDisk = usesDisk;
        this.name = name;
    }
}
