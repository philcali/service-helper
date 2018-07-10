package me.philcali.service.binding.cookie;

import java.util.Date;

public interface ICookie {
    String getName();

    String getValue();

    String getDomain();

    String getPath();

    Date getExpires();

    long getMaxAge();

    boolean isSecure();

    boolean isHttpOnly();
}
