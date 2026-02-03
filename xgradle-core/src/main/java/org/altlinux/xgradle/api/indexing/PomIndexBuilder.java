package org.altlinux.xgradle.api.indexing;

import java.nio.file.Path;
import java.util.List;

public interface PomIndexBuilder {
    PomIndex build(List<Path> pomFiles);
}
