package me.philcali.service.netty;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import me.philcali.service.reflection.system.SystemRequestRouterBuilder;

public class App {
    @Parameter(names = { "--jar", "-j" }, required = true)
    private String jarFile;

    @Parameter(names = { "--port", "-p" }, required = false)
    private int port = 8000;

    public static void main(final String[] args) throws Exception {
        final App app = new App();
        JCommander.newBuilder()
                .acceptUnknownOptions(false)
                .addObject(app)
                .args(args)
                .build();
        app.run();
    }

    public void run() throws Exception {
        new Service(new ServiceInitializer(SystemRequestRouterBuilder.defaultRouter(jarFile))).start(port);
    }
}
