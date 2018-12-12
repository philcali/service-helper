package me.philcali.service.reflection.filter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

public class Base64Decode implements Function<String, String> {
    @Override
    public String apply(final String input) {
        return new String(Base64.getUrlDecoder().decode(input), StandardCharsets.UTF_8);
    }
}
