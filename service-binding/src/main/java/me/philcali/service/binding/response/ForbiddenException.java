package me.philcali.service.binding.response;

public class ForbiddenException extends HttpException {
    private static final long serialVersionUID = 4334369301239128597L;

    public ForbiddenException() {
        super(403);
    }
}
