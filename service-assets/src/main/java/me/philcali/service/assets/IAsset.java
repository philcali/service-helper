package me.philcali.service.assets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public interface IAsset {
    String getMimeType();
    Date getLastModified();
    InputStream getContent() throws IOException;

    default String getContentAsString() throws IOException {
        try (final InputStream stream = getContent()) {
            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                final byte[] buffer = new byte[8024];
                int read = -1;
                while ((read = stream.read(buffer)) > 0) {
                    bos.write(buffer, 0, read);
                }
                return new String(bos.toByteArray(), StandardCharsets.UTF_8);
            }
        }
    }
}
