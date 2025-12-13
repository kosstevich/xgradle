package org.altlinux.xgradle.api.caches;

import com.google.common.collect.ImmutableList;

import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.logging.Logger;

import java.util.Map;

public interface PomDataCache {
    MavenCoordinate getPom(String key);

    void putPom(String key, MavenCoordinate coordinate);

    void invalidatePom(String key);

    ImmutableList<MavenCoordinate> getDependencyManagement(String key);
    void putDependencyManagement(String key, ImmutableList<MavenCoordinate> dependencies);

    ImmutableList<MavenCoordinate> getDependencies(String key);
    void putDependencies(String key, ImmutableList<MavenCoordinate> dependencies);

    Map<String, String> getProperties(String key);
    void putProperties(String key, Map<String, String> properties);

    void logStats(Logger logger);
}
