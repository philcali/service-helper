package me.philcali.service.binding.cookie;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TimeZone;

public class CookieEncoder {
    public String encode(final ICookie cookie) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final StringJoiner builder = new StringJoiner("; ");
        builder.add(cookie.getName() + "=" + cookie.getValue());
        Optional.ofNullable(cookie.getDomain())
                .ifPresent(domain -> builder.add("Domain=" + domain));
        Optional.ofNullable(cookie.getPath())
                .ifPresent(path -> builder.add("Path=" + path));
        Optional.ofNullable(cookie.getMaxAge())
                .filter(age -> age > 0)
                .ifPresent(age -> builder.add("Max-Age=" + age));
        Optional.ofNullable(cookie.getExpires())
                .ifPresent(expires -> builder.add("Expires=" + sdf.format(expires)));
        if (cookie.isSecure()) {
            builder.add("Secure");
        }
        if (cookie.isHttpOnly()) {
            builder.add("HttpOnly");
        }
        return builder.toString();
    }
}
