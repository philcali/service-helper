package me.philcali.service.binding.response;

public class HttpException extends RuntimeException {
    private static final long serialVersionUID = 8731945000715004944L;
    private final int statusCode;

    public HttpException(final int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public HttpException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
