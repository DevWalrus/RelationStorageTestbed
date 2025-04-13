package Graphs.Disk;

public interface GraphRecord {
    int getRecordSize();
    /**
     * Returns the record as a byte array.
     */
    byte[] toBytes();

    /**
     * Populates fields from the provided byte array.
     */
    void fromBytes(byte[] bytes);
}
