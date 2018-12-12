package me.philcali.service.reflection.parameter.exception;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1302136622457746406L;

    public ValidationException(final String message) {
        super(message);
    }
}
