package Exceptions;

public class NoProjectsException extends Exception {
    public NoProjectsException() {
    }

    public NoProjectsException(String message) {
        super(message);
    }
}
