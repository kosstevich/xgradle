package org.altlinux.xgradle.api.managers;

import org.altlinux.xgradle.impl.model.MavenCoordinate;
import java.util.Map;
import java.util.Set;

public interface TransitiveDependencyManager {

    void processTransitiveDependencies(Map<String, MavenCoordinate> systemArtifacts);

    Set<String> getSkippedDependencies();
}
