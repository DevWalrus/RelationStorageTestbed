public enum GraphType {
    NEO4J(false),
    ADJ_MATRIX(false),
    ADJ_CNT_MATRIX(false),
    EDGE_LIST(false),
    SIMPLE(false),
    EDGE_LIST_DISK(true),
    LINKED_LIST_DISK(true);

    public final boolean usesDisk;

    GraphType(boolean usesDisk) {
        this.usesDisk = usesDisk;
    }
}
