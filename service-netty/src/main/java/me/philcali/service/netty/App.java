package me.philcali.service.netty;

import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import me.philcali.service.reflection.system.CachingComponentProvider;
import me.philcali.service.reflection.system.DynamicModuleComponentProvider;
import me.philcali.service.reflection.system.IComponentProvider;
import me.philcali.service.reflection.system.SystemRequestRouterBuilder;

public class App {
    @Parameter(names = { "--jar", "-j" }, required = true)
    private List<String> jarFiles;

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
        final IComponentProvider provider = new CachingComponentProvider(new DynamicModuleComponentProvider(jarFiles, null));
        new Service(new ServiceInitializer(new SystemRequestRouterBuilder()
                .withComponentProvider(provider)
                .build(), provider))
        .start(port);
    }
}
