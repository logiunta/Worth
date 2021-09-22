package Exceptions;

public class CardAlreadyExistException extends Exception {
    public CardAlreadyExistException() {
    }

    public CardAlreadyExistException(String message) {
        super(message);
    }
}
