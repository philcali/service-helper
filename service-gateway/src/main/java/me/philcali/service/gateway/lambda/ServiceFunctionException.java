package me.philcali.service.gateway.lambda;

public class ServiceFunctionException extends RuntimeException {
    private static final long serialVersionUID = -5719268463098525704L;

    public ServiceFunctionException(final Throwable ex) {
        super(ex);
    }
}
