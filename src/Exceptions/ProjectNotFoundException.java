package Exceptions;

public class ProjectNotFoundException extends Exception {
    public ProjectNotFoundException() {
    }

    public ProjectNotFoundException(String message) {
        super(message);
    }
}
