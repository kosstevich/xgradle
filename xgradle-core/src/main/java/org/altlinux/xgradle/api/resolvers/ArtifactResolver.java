package org.altlinux.xgradle.api.resolvers;

import org.altlinux.xgradle.impl.model.MavenCoordinate;
import org.gradle.api.logging.Logger;

import java.util.Map;
import java.util.Set;

public interface ArtifactResolver {
    void resolve(Set<String> dependencies, Logger logger);

    void filter();

    Map<String, MavenCoordinate> getSystemArtifacts();

    Set<String> getNotFoundDependencies();
}
