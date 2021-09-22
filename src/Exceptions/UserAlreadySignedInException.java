package Exceptions;

public class UserAlreadySignedInException extends Exception {
    public UserAlreadySignedInException() {
    }

    public UserAlreadySignedInException(String message) {
        super(message);
    }
}
