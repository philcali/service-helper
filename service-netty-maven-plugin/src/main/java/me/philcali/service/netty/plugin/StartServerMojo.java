package me.philcali.service.netty.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PACKAGE)
@Execute(goal = "download")
public class StartServerMojo extends AbstractMojo {
    @Parameter(property = "service.artifact", defaultValue = "${project.build.directory}/service-netty.jar")
    private String artifact;

    @Parameter(property = "service.module", defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}.jar", required = true)
    private String module;

    @Parameter(property = "service.port", defaultValue = "8000")
    private int port;

    @Parameter(property = "service.pid", defaultValue = "${project.name}")
    private String pid;

    @Parameter(property = "service.jvmArgs")
    private String[] jvmArgs;

    @Parameter(property = "service.java.home", defaultValue = "${java.home}", required = true)
    private String javaHome;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<String> commands = new ArrayList<>();
        commands.add("java");
        commands.add("-D" + pid);
        Arrays.stream(jvmArgs).map(arg -> "-D" + arg).forEach(commands::add);
        commands.add("-jar");
        commands.add(artifact);
        commands.add("--jar");
        commands.add(module);
        commands.add("--port");
        commands.add(Integer.toString(port));
        try {
            getLog().info("Starting local netty service with: " + commands);
            final Process server = new ProcessBuilder(commands)
                    .directory(new File(javaHome, "bin"))
                    .start();
            if (server.isAlive()) {
                getLog().info("Running netty server on port " + port);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to start local netty service: " + e.getMessage());
        }
    }
}
