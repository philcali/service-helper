package me.philcali.service.binding.response;

public class NotFoundException extends HttpException {
    private static final long serialVersionUID = 8887323558568165103L;

    public NotFoundException() {
        super(404);
    }

}
