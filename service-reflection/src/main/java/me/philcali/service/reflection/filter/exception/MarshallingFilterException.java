package me.philcali.service.reflection.filter.exception;

public class MarshallingFilterException extends RuntimeException {
    private static final long serialVersionUID = 3153237131623336633L;

    public MarshallingFilterException(final Throwable ex) {
        super(ex);
    }
}
