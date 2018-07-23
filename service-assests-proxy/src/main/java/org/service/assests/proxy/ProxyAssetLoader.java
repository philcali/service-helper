package org.service.assests.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;

import javax.inject.Inject;

import me.philcali.http.api.HttpMethod;
import me.philcali.http.api.IHttpClient;
import me.philcali.http.api.IRequest;
import me.philcali.http.api.IResponse;
import me.philcali.http.api.util.URLBuilder;
import me.philcali.service.assets.IAsset;
import me.philcali.service.assets.IAssetLoader;

public class ProxyAssetLoader implements IAssetLoader {
    private final IHttpClient client;
    private final IProxyLocationConfig config;

    @Inject
    public ProxyAssetLoader(final IHttpClient client, final IProxyLocationConfig config) {
        this.client = client;
        this.config = config;
    }

    private String getUrl(final String ... paths) {
        final StringJoiner path = new StringJoiner("/");
        Optional.ofNullable(config.getPath()).ifPresent(path::add);
        Arrays.stream(paths).filter(p -> !p.isEmpty()).forEach(path::add);
        return new URLBuilder()
                .withProtocol(config.getProtocol())
                .withHost(config.getHost())
                .withPort(config.getPort())
                .withPath(path.toString())
                .build()
                .toString();
    }

    @Override
    public IAsset load(final String ... parts) {
        final IRequest request = client.createRequest(HttpMethod.GET, getUrl(parts));
        final IResponse response = request.respond();
        return new IAsset() {

            @Override
            public String getMimeType() {
                return response.header("Content-Type");
            }

            @Override
            public Date getLastModified() {
                final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                try {
                    return sdf.parse(response.header("Date"));
                } catch (ParseException e) {
                    return new Date();
                }
            }

            @Override
            public InputStream getContent() throws IOException {
                return response.body();
            }
        };
    }

}
