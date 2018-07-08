package me.philcali.service.reflection.exception;

public class ResourceMethodCreationException extends RuntimeException {
    private static final long serialVersionUID = -4510055560146644073L;

    public ResourceMethodCreationException(final String message, final Throwable t) {
        super(message, t);
    }
}
