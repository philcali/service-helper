package me.philcali.service.netty.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import me.philcali.service.netty.Service;
import me.philcali.service.netty.ServiceInitializer;
import me.philcali.service.reflection.system.SystemRequestRouterBuilder;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PACKAGE)
public class StartServerMojo extends AbstractMojo {
    @Parameter(property = "service.module.artifact", defaultValue = "${project.build.outputDirectory}/${project.artifactId}-${project.version}.jar", required = true)
    private String jarFile;

    @Parameter(property = "service.local.port", defaultValue = "8000")
    private int port;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Using " + jarFile + " module");
        final Service service = new Service(new ServiceInitializer(SystemRequestRouterBuilder.defaultRouter(jarFile)));
        try {
            getLog().info("Starting netty local server on " + port);
            service.start(port);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Failed to start netty local server: " + e.getMessage());
        }
    }
}
