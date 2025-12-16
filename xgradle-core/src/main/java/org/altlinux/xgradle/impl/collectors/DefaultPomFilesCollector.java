package org.altlinux.xgradle.impl.collectors;

import com.google.inject.Singleton;
import org.altlinux.xgradle.api.collectors.PomFilesCollector;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of PomFilesCollector.
 *
 * Scans the filesystem and returns all .pom files
 * under the specified root directory.
 */
@Singleton
public class DefaultPomFilesCollector implements PomFilesCollector {

    private static final int MAX_SEARCH_DEPTH = 10;

    /**
     * Collects all POM files under the given root directory.
     *
     * @param rootDirectory root directory to scan
     * @param logger logger for diagnostic messages
     * @return list of POM file paths
     */
    @Override
    public List<Path> collectPomFiles(Path rootDirectory, Logger logger) {
        List<Path> pomPaths = new ArrayList<>();

        if (rootDirectory == null || !Files.isDirectory(rootDirectory)) {
            logger.lifecycle("POM root directory is not valid: {}", rootDirectory);
            return pomPaths;
        }

        try (var stream = Files.walk(rootDirectory, MAX_SEARCH_DEPTH)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".pom"))
                    .forEach(pomPaths::add);
        } catch (IOException e) {
            logger.lifecycle("Error while scanning POM directory {}: {}", rootDirectory, e.getMessage());
        }

        return pomPaths;
    }
}
