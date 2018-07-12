package me.philcali.service.netty.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import me.philcali.http.api.HttpMethod;
import me.philcali.http.api.IHttpClient;
import me.philcali.http.api.IResponse;
import me.philcali.http.api.exception.HttpException;
import me.philcali.http.java.NativeHttpClient;

@Mojo(name = "download", requiresOnline = true)
public class DownloadMojo extends AbstractMojo {
    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    @Parameter(property = "download.artifact", defaultValue = "http://philcali.me/projects/service/service-netty-0.0.1-SNAPSHOT.jar")
    private String artifact;

    @Parameter(property = "download.output", defaultValue = "${project.build.directory}/service-netty.jar")
    private String output;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final IHttpClient client = new NativeHttpClient();
        final Optional<Path> filePath = getExistingArtifact().filter(file -> isLatest(client, file));
        if (!filePath.isPresent()) {
            downloadFile(client);
        }

    }

    private boolean isLatest(final IHttpClient client, final Path file) {
        try {
            final IResponse response = client.createRequest(HttpMethod.HEAD, artifact).respond();
            final SimpleDateFormat sdf = new SimpleDateFormat(HTTP_DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            final Date lastModified = sdf.parse(response.header("last-modified"));
            return Files.getLastModifiedTime(file).toMillis() >= lastModified.getTime();
        } catch (HttpException
                | IOException
                | ParseException e) {
            getLog().warn("Failed o check archive", e);
            return true;
        }
    }

    private void downloadFile(final IHttpClient client) throws MojoFailureException {
        try {
            getLog().info("Downloading executable from " + artifact);
            final IResponse response = client.createRequest(HttpMethod.GET, artifact).respond();
            if (response.status() == 200) {
                Files.copy(response.body(), Paths.get(output), StandardCopyOption.REPLACE_EXISTING);
            } else {
                throw new RuntimeException("Failed to download artifact: " + response.status());
            }
        } catch (HttpException | IOException e) {
            throw new MojoFailureException("Failed to save artifact!", e);
        }
    }

    private Optional<Path> getExistingArtifact() {
        return Optional.ofNullable(Paths.get(output)).filter(Files::exists);
    }
}
