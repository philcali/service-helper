package me.philcali.service.binding.cookie;

import java.util.Date;

public class CookieImpl implements ICookie {
    public static final class Builder {
        private String name;
        private String value;
        private String domain;
        private String path;
        private Date expires;
        private long maxAge;
        private boolean secure;
        private boolean httpOnly;

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }

        public Builder withDomain(final String domain) {
            this.domain = domain;
            return this;
        }

        public Builder withPath(final String path) {
            this.path = path;
            return this;
        }

        public Builder withExpires(final Date expires) {
            this.expires = expires;
            return this;
        }

        public Builder withMaxAge(final long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Builder withSecure(final boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder withHttpOnly(final boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        public ICookie build() {
            return new CookieImpl(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String name;
    private final String value;
    private final String domain;
    private final String path;
    private final Date expires;
    private final long maxAge;
    private final boolean secure;
    private final boolean httpOnly;

    private CookieImpl(final Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.domain = builder.domain;
        this.path = builder.path;
        this.expires = builder.expires;
        this.maxAge = builder.maxAge;
        this.secure = builder.secure;
        this.httpOnly = builder.httpOnly;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Date getExpires() {
        return expires;
    }

    @Override
    public long getMaxAge() {
        return maxAge;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public boolean isHttpOnly() {
        return httpOnly;
    }
}
