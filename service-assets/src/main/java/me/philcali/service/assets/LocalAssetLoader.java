package me.philcali.service.assets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

public class LocalAssetLoader implements IAssetLoader {
    private static final String DEFAULT_ASSET_ROOT = "/web_assets";
    private final String assetRoot;

    public LocalAssetLoader(final String assetRoot) {
        this.assetRoot = assetRoot;
    }

    public LocalAssetLoader() {
        this(DEFAULT_ASSET_ROOT);
    }

    public static final class LocalAsset implements IAsset {
        private final String fullPath;
        private final URL fileUrl;

        public LocalAsset(final String fullPath) {
            this.fullPath = fullPath;
            this.fileUrl = getClass().getResource(fullPath);
            Objects.requireNonNull(fileUrl, "Resource with " + fullPath + " does not exist!");
        }

        @Override
        public String getMimeType() {
            return URLConnection.guessContentTypeFromName(fullPath);
        }

        @Override
        public Date getLastModified() {
            // In the form of jar:file:/path/to/app.jar!{fullPath}
            final File jarFile = new File(fileUrl.getFile()
                    .replace("jar:file:/", "")
                    .replaceAll("!.+$", ""));
            return new Date(jarFile.lastModified());
        }

        @Override
        public InputStream getContent() throws IOException {
            return fileUrl.openStream();
        }
    }

    @Override
    public IAsset load(final String type, final String name) {
        return new LocalAsset(new StringJoiner("/").add(assetRoot).add(type).add(name).toString());
    }
}
