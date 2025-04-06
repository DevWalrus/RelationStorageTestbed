package Exceptions;

public class LongTooLargeException extends Exception {
    public LongTooLargeException(String message) {
        super(message);
    }

    public LongTooLargeException() {
        super();
    }
}
