package org.altlinux.xgradle.impl.maven;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.maven.PomFilenameMatcher;
import org.altlinux.xgradle.api.maven.PomFinder;
import org.altlinux.xgradle.api.maven.PomHierarchyLoader;

public final class MavenModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PomFilenameMatcher.class).to(MavenPomFilenameMatcher.class);
        bind(PomFinder.class).to(MavenPomFinder.class);
        bind(PomHierarchyLoader.class).to(MavenPomHierarchyLoader.class);
    }
}
