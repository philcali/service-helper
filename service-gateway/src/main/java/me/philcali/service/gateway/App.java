package me.philcali.service.gateway;

import com.beust.jcommander.JCommander;

public class App {
    public static void main(final String[] args) {
        final AppContext context = new AppContext();
        JCommander.newBuilder()
                .addObject(context)
                .acceptUnknownOptions(false)
                .build()
                .parse(args);
        final ApiManifest manifest = ApiManifest.builder()
                .withContext(context)
                .build();
        manifest.upsert();
    }
}
