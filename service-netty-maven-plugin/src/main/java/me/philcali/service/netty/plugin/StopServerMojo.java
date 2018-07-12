package me.philcali.service.netty.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "stop")
public class StopServerMojo extends AbstractMojo {
    private static final Pattern PID_REGEX = Pattern.compile("^(\\d+).*");

    @Parameter(property = "service.pid", defaultValue = "${project.name}")
    private String processLookup;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        findStartServer().ifPresent(this::killServer);
    }

    private Optional<String> findStartServer() throws MojoExecutionException {
        try {
            final Process process = new ProcessBuilder("jps", "-v").start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String pid = null;
            String line = reader.readLine();
            while (line != null) {
                final Matcher matcher = PID_REGEX.matcher(line);
                if (matcher.find() && line.contains(processLookup)) {
                    pid = matcher.group(1);
                }
                line = reader.readLine();
            }
            return Optional.ofNullable(pid);
        } catch (IOException ie) {
            throw new MojoExecutionException(ie.getMessage());
        }
    }

    private void killServer(final String pid) {
        try {
            final Process process = new ProcessBuilder("kill", "-9", pid).start();
            if (process.waitFor(5, TimeUnit.SECONDS)) {
                getLog().info("Exit value for service-netty:start: " + process.exitValue());
            }
        } catch (IOException | InterruptedException ie) {
            getLog().error("Failed to stop ddb:start", ie);
        }
    }
}
