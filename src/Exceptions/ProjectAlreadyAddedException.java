package Exceptions;

public class ProjectAlreadyAddedException extends Exception {
    public ProjectAlreadyAddedException() {
    }

    public ProjectAlreadyAddedException(String message) {
        super(message);
    }
}
