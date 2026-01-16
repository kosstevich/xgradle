package org.altlinux.xgradle.api.indexing;

import org.altlinux.xgradle.impl.model.MavenCoordinate;

import java.nio.file.Path;
import java.util.List;

public interface PomIndexBuilder {
    PomIndex build(List<Path> pomFiles);
}
