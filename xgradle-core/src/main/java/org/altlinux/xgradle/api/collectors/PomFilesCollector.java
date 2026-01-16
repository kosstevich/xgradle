package org.altlinux.xgradle.api.collectors;

import java.nio.file.Path;
import java.util.List;

/**
 * Collector for POM files in a repository directory.
 * <p>
 * Responsible for scanning the filesystem and returning
 * a list of all POM files under the given root directory.
 */
public interface PomFilesCollector extends Collector<Path, List<Path>> {

    @Override
    List<Path> collect(Path rootDirectory);
}
