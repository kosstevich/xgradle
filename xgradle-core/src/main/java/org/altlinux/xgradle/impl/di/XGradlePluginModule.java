package org.altlinux.xgradle.impl.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.altlinux.xgradle.api.caches.PomDataCache;
import org.altlinux.xgradle.api.managers.ScopeManager;
import org.altlinux.xgradle.api.managers.TransitiveDependencyManager;
import org.altlinux.xgradle.api.maven.PomFinder;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.caches.DefaultPomDataCache;
import org.altlinux.xgradle.impl.managers.DefaultTransitiveDependencyManager;
import org.altlinux.xgradle.impl.managers.MavenScopeManager;
import org.altlinux.xgradle.impl.maven.DefaultPomFinder;
import org.altlinux.xgradle.impl.maven.MavenPomFilenameMatcher;
import org.altlinux.xgradle.impl.services.DefaultPomParser;

public class XGradlePluginModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(PomDataCache.class).to(DefaultPomDataCache.class);

        bind(PomParser.class)
                .annotatedWith(Names.named("Default"))
                .to(DefaultPomParser.class);

        bind(PomFinder.class)
                .annotatedWith(Names.named("Default"))
                .to(DefaultPomFinder.class);

        bind(MavenPomFilenameMatcher.class).in(Singleton.class);

        bind(ScopeManager.class)
                .annotatedWith(Names.named("Maven"))
                .to(MavenScopeManager.class);

        bind(TransitiveDependencyManager.class).to(DefaultTransitiveDependencyManager.class);
    }
}
