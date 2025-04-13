package Graphs.Disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.function.Supplier;

/**
 * A wrapper around RandomAccessFile that reserves the first four bytes
 * to store a count (which can be set, retrieved, or incremented) and provides
 * methods to perform common random access operations.
 */
public class GraphRandomAccessFile<T extends GraphRecord> {
    private RandomAccessFile raf;
    private final File rafFile;

    /**
     * Creates a GraphRandomAccessFile for the given file path.
     * If the file does not exist, it will be created (along with any missing parent directories)
     * and initialized with a count of 0.
     *
     * @param filePath the file path
     * @throws IOException if an I/O error occurs.
     */
    public GraphRandomAccessFile(String filePath) throws IOException {
        this(new File(filePath));
    }

    /**
     * Creates a GraphRandomAccessFile for the given file.
     * If the file does not exist, it will be created (along with any missing parent directories)
     * and initialized with a count of 0.
     *
     * @param file the file to open
     * @throws IOException if an I/O error occurs.
     */
    public GraphRandomAccessFile(File file) throws IOException {
        rafFile = file;
        if (!rafFile.exists()) {
            File parent = rafFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            raf = new RandomAccessFile(rafFile, "rw");
            // Initialize the count to 0 at the beginning of the file.
            setCount(0);
        } else {
            raf = new RandomAccessFile(rafFile, "rw");
        }
    }

    /**
     * Reads the count stored at the beginning of the file.
     *
     * @return the current count.
     * @throws IOException if an I/O error occurs.
     */
    public int getCount() throws IOException {
        long currentPos = raf.getFilePointer();
        raf.seek(0);
        int count = raf.readInt();
        raf.seek(currentPos);
        return count;
    }

    /**
     * Sets the count at the beginning of the file.
     *
     * @param count the new count value.
     * @throws IOException if an I/O error occurs.
     */
    public void setCount(int count) throws IOException {
        long currentPos = raf.getFilePointer();
        raf.seek(0);
        raf.writeInt(count);
        raf.seek(currentPos);
    }

    /**
     * Increments the count stored at the beginning of the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void incCount() throws IOException {
        long currentPos = raf.getFilePointer();
        raf.seek(0);
        int count = raf.readInt();
        raf.seek(0);
        raf.writeInt(count + 1);
        raf.seek(currentPos);
    }

    /**
     * Wipes the data in this file and sets up a new file with a count of 0.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void clear() throws IOException {
        raf.close();
        Files.deleteIfExists(rafFile.toPath());
        raf = new RandomAccessFile(rafFile, "rw");
        setCount(0);
    }

    // Delegate methods for additional RandomAccessFile functionality

    public void writeElement(T element) throws IOException {
        raf.write(element.toBytes());
    }

    public T readElement(Supplier<T> supplier) throws IOException {
        T element = supplier.get();
        byte[] data = new byte[element.getRecordSize()];
        raf.readFully(data);
        element.fromBytes(data);
        return element;
    }

    /**
     * Seeks to the specified position in the file.
     *
     * @param pos the position to seek to.
     * @throws IOException if an I/O error occurs.
     */
    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    /**
     * Seeks to the end of the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void seekTheEnd() throws IOException {
        raf.seek(raf.length());
    }

    /**
     * Skip over a number of bytes.
     *
     * @param size the number of bytes to skip over.
     * @throws IOException if an I/O error occurs.
     */
    public void skipBytes(int size) throws IOException {
        raf.skipBytes(size);
    }

    /**
     * Skip over a number of bytes.
     *
     * @param size the number of bytes to skip over.
     * @throws IOException if an I/O error occurs.
     */
    public void skipBytes(long size) throws IOException {
        if (size > Integer.MAX_VALUE) throw new RuntimeException("The size you're trying to skip is bigger than an int.");
        if (size < Integer.MIN_VALUE) throw new RuntimeException("The size you're trying to skip is smaller than an int.");
        skipBytes((int) size);
    }

    /**
     * Returns the current file pointer.
     *
     * @return the offset from the beginning of the file, in bytes.
     * @throws IOException if an I/O error occurs.
     */
    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    /**
     * Writes an array of bytes to the file at the current file pointer.
     *
     * @param b the array of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    /**
     * Writes a boolean to the file at the current file pointer.
     *
     * @param b the boolean to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeBoolean(boolean b) throws IOException {
        raf.writeBoolean(b);
    }

    /**
     * Reads a boolean from the file at the current file pointer.
     *
     * @return the boolean read.
     * @throws IOException if an I/O error occurs.
     */
    public boolean readBoolean() throws IOException {
        return raf.readBoolean();
    }

    /**
     * Writes an int to the file at the current file pointer.
     *
     * @param i the int to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeInt(int i) throws IOException {
        raf.writeInt(i);
    }

    /**
     * Reads an int from the file at the current file pointer.
     *
     * @return the int read.
     * @throws IOException if an I/O error occurs.
     */
    public int readInt() throws IOException {
        return raf.readInt();
    }

    /**
     * Writes a long to the file at the current file pointer.
     *
     * @param l the long to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeLong(long l) throws IOException {
        raf.writeLong(l);
    }

    /**
     * Reads a long from the file at the current file pointer.
     *
     * @return the long read.
     * @throws IOException if an I/O error occurs.
     */
    public long readLong() throws IOException {
        return raf.readLong();
    }

    /**
     * Writes a UTF string to the file at the current file pointer.
     *
     * @param str the string to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeUTF(String str) throws IOException {
        raf.writeUTF(str);
    }

    /**
     * Reads a UTF string from the file at the current file pointer.
     *
     * @return the string read.
     * @throws IOException if an I/O error occurs.
     */
    public String readUTF() throws IOException {
        return raf.readUTF();
    }

    /**
     * Returns the length of the file.
     *
     * @return the length of the file, in bytes.
     * @throws IOException if an I/O error occurs.
     */
    public long length() throws IOException {
        return raf.length();
    }

    /**
     * Closes the underlying RandomAccessFile.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        raf.close();
    }
}
