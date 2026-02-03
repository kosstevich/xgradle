package org.altlinux.xgradle.api.indexing;

import org.altlinux.xgradle.impl.model.MavenCoordinate;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PomIndex {

    void build(Path rootDirectory);

    void build(List<Path> pomFiles);

    Optional<MavenCoordinate> find(String groupId, String artifactId);

    List<MavenCoordinate> findAllForGroup(String groupId);

    Map<String, MavenCoordinate> snapshot();
}
