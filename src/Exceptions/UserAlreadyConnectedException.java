package Exceptions;

public class UserAlreadyConnectedException extends Exception {
    public UserAlreadyConnectedException() {
    }

    public UserAlreadyConnectedException(String message) {
        super(message);
    }
}
