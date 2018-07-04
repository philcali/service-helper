package me.philcali.service.binding.auth;

public class BearerTokenFilter implements ITokenFilter {
    private static final String BEARER_REGEX = "Bearer\\s*";

    @Override
    public String apply(final String token) {
        return token.replaceAll(BEARER_REGEX, "");
    }

}
