package me.philcali.service.binding.cookie;

import java.util.ArrayList;
import java.util.List;

public class CookieDecoder {
    public List<ICookie> decode(final String headerValue) {
        final List<ICookie> cookies = new ArrayList<>();
        final String[] cookieValues = headerValue.split(";\\s*");
        for (final String cookieValue : cookieValues) {
            final int index = cookieValue.indexOf('=');
            final String name = cookieValue.substring(0, index + 1);
            final String value = cookieValue.substring(index, cookieValue.length());
            cookies.add(CookieImpl.builder().withName(name).withValue(value).build());
        }
        return cookies;
    }
}
