package me.philcali.service.assets;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

import me.philcali.service.annotations.GET;
import me.philcali.service.annotations.request.HeaderParam;
import me.philcali.service.annotations.request.PathParam;
import me.philcali.service.binding.response.HttpException;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.binding.response.Response;

public class AssetResource {
    public static final String MAX_AGE_NAME = "asset.max.age";
    private final IAssetLoader resources;
    private final int maxAge;

    @Inject
    public AssetResource(final IAssetLoader resources, @Named(MAX_AGE_NAME) final int maxAge) {
        this.resources = resources;
        this.maxAge = maxAge;
    }

    @GET("/")
    public IResponse getIndex(@HeaderParam("If-Modified-Since") final String requestModified) {
        return getResource(requestModified, "html", "index.html");
    }

    @GET("/asset/{type}/{name}")
    public IResponse getResource(
            @HeaderParam("If-Modified-Since") final String requestModified,
            @PathParam final String type,
            @PathParam final String name) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date cacheDate = null;
            if (Objects.nonNull(requestModified)) {
                cacheDate = sdf.parse(requestModified);
            }
            final IAsset asset = resources.load(type, name);
            final Date lastModified = asset.getLastModified();
            return Optional.ofNullable(cacheDate)
                    .filter(lastModified::before)
                    .map(date -> Response.notModified())
                    .orElseGet(() -> {
                        try {
                            final String content = asset.getContentAsString();
                            return Response.builder()
                                .withBody(content)
                                .withHeaders("Content-Type", asset.getMimeType())
                                .withHeaders("Cache-Control", "public, mag-age: " + maxAge)
                                .withHeaders("Last-Modified", sdf.format(lastModified))
                                .withHeaders("Content-Length", Integer.toString(content.length()))
                                .build();
                        } catch (IOException ie) {
                            throw new HttpException(500, "Unable to load " + name);
                        }
                    });
        } catch (NullPointerException npe) {
            throw new HttpException(404, "Missing resource " + name);
        } catch (ParseException pe) {
            throw new HttpException(400, "Invalid client request date!");
        }
    }
}
