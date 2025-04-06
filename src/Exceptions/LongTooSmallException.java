package Exceptions;

public class LongTooSmallException extends Exception {
    public LongTooSmallException(String message) {
        super(message);
    }

    public LongTooSmallException() {
        super();
    }
}
