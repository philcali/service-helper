package me.philcali.service.binding.response;

public class UnauthorizedException extends HttpException {
    private static final long serialVersionUID = 6058731632936560167L;

    public UnauthorizedException() {
        super(401);
    }

    public UnauthorizedException(final String message) {
        super(401, message);
    }
}
