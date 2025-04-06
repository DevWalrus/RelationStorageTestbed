package Exceptions;

public class InvalidNodeAccessException extends Exception {
    public InvalidNodeAccessException(String message) {
        super(message);
    }

    public InvalidNodeAccessException() {
        super();
    }
}
