package me.philcali.service.reflection.system;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import me.philcali.service.reflection.IModule;

public class DynamicModuleComponentProvider implements IComponentProvider {
    private final List<String> jarFiles;
    private final ClassLoader loader;

    public DynamicModuleComponentProvider(final List<String> jarFiles, final ClassLoader loader) {
        this.jarFiles = jarFiles;
        this.loader = loader;
    }

    @Override
    public List<Object> getComponents() {
        final ClassLoader classLoader = new URLClassLoader(getModuleURLs(), Optional
                .ofNullable(loader)
                .orElseGet(ClassLoader::getSystemClassLoader));
        final ServiceLoader<IModule> moduleLoader = ServiceLoader.load(IModule.class, classLoader);
        return StreamSupport.stream(moduleLoader.spliterator(), false)
                .flatMap(module -> module.getComponents().stream())
                .collect(Collectors.toList());
    }

    private URL[] getModuleURLs() {
        try {
            final URL[] urls = new URL[jarFiles.size()];
            for (int index = 0; index < jarFiles.size(); index++) {
                urls[index] = new File(jarFiles.get(0)).toURI().toURL();
            }
            return urls;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
