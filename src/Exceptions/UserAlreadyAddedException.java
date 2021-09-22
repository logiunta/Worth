package Exceptions;

public class UserAlreadyAddedException extends Exception {
    public UserAlreadyAddedException() {
        super();
    }

    public UserAlreadyAddedException(String message) {
        super(message);
    }
}
