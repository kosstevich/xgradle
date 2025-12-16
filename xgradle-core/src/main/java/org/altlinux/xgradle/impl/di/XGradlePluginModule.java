package org.altlinux.xgradle.impl.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.caches.PomDataCache;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.caches.DefaultPomDataCache;
import org.altlinux.xgradle.impl.maven.MavenPomFilenameMatcher;
import org.altlinux.xgradle.impl.services.DefaultPomParser;

public class XGradlePluginModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(PomDataCache.class).to(DefaultPomDataCache.class);

        bind(PomParser.class).to(DefaultPomParser.class);

        bind(MavenPomFilenameMatcher.class).in(Singleton.class);

        bind(PomParser.class).to(DefaultPomParser.class);
    }
}
