package me.philcali.service.binding.response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import me.philcali.service.binding.cookie.CookieEncoder;
import me.philcali.service.binding.cookie.ICookie;

public class Response implements IResponse {
    private static final CookieEncoder DEFAULT_ENCODER = new CookieEncoder();

    public static class Builder {
        private int statusCode;
        private Map<String, String> headers;
        private String body;
        private Throwable exception;
        private boolean raw = false;

        public Response build() {
            return new Response(this);
        }

        public String getBody() {
            return body;
        }

        public Throwable getException() {
            return exception;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isRaw() {
            return raw;
        }

        public Builder withBody(final String body) {
            this.body = body;
            return this;
        }

        public Builder withException(final Throwable exception) {
            this.exception = exception;
            return this;
        }

        public Builder withHeaders(final Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withRaw(final boolean raw) {
            this.raw = raw;
            return this;
        }

        public Builder withCookies(final ICookie ... cookies) {
            // This is a last write win situation, regarding the limitations of API gateway
            Arrays.stream(cookies).map(DEFAULT_ENCODER::encode).forEach(encoded -> {
                withHeaders("set-cookie", encoded);
            });
            return this;
        }

        public Builder withHeaders(final String name, final String value) {
            final Map<String, String> newHeaders = Optional.ofNullable(headers).orElseGet(HashMap::new);
            newHeaders.put(name, value);
            withHeaders(newHeaders);
            return this;
        }

        public Builder withStatusCode(final int statusCode) {
            this.statusCode = statusCode;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder().withStatusCode(200).withHeaders("Content-Length", "0");
    }

    public static Builder redirect(final String location) {
        return builder().withStatusCode(302).withHeaders("Location", location);
    }

    public static Response forbidden() {
        return builder().withStatusCode(403).build();
    }

    public static Builder from(IResponse response) {
        return builder()
                .withRaw(response.isRaw())
                .withBody(response.getBody())
                .withException(response.getException())
                .withStatusCode(response.getStatusCode())
                .withHeaders(response.getHeaders());
    }

    public static Response internalError(Throwable ex) {
        return builder().withException(ex).withStatusCode(500).build();
    }

    public static Response noContent() {
        return builder().withStatusCode(204).build();
    }

    public static Response notFound() {
        return builder().withStatusCode(404).withHeaders("Content-Length", "0").build();
    }

    public static Response ok() {
        return builder().build();
    }

    public static Response ok(final String content) {
        return builder().withBody(content).build();
    }

    public static Response unauthorized() {
        return builder().withStatusCode(401).build();
    }

    public static Response notModified() {
        return builder().withStatusCode(304).build();
    }

    private final int statusCode;
    private final Map<String, String> headers;
    private final String body;
    private final boolean raw;
    private final Throwable exception;

    private Response(final Builder builder) {
        this.raw = builder.raw;
        this.statusCode = builder.getStatusCode();
        this.headers = builder.getHeaders();
        this.body = builder.getBody();
        this.exception = builder.getException();
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public boolean isRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return "Response[body=" + body + ",headers=" + headers + ",statusCode=" + statusCode + "]";
    }
}
