package me.philcali.service.reflection.filter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

public class Base64Encode implements Function<String, String> {
    @Override
    public String apply(final String input) {
        return Base64.getUrlEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}
