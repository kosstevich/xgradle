/*
 * Copyright 2025 BaseALT Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.altlinux.xgradle.impl.indexing;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.collectors.PomFilesCollector;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.api.indexing.PomIndex;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.gradle.util.internal.VersionNumber;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of PomIndex.
 * <p>
 * Collects all POM files under the given root directory,
 * parses them in parallel and builds a map from
 * "groupId:artifactId" to MavenCoordinate.
 */
@Singleton
public class DefaultPomIndex implements PomIndex {

    private static final Logger logger = Logging.getLogger(DefaultPomIndex.class);

    private final PomFilesCollector pomFilesCollector;
    private final PomParser pomParser;

    /**
     * Creates a new DefaultPomIndex.
     *
     * @param pomFilesCollector collector that scans repository for POM files
     * @param pomParser parser used to read POM contents
     */
    @Inject
    public DefaultPomIndex(PomFilesCollector pomFilesCollector, PomParser pomParser) {
        this.pomFilesCollector = pomFilesCollector;
        this.pomParser = pomParser;
    }

    /**
     * Builds an index of Maven coordinates for the given dependency keys.
     * <p>
     * POM files are collected once and parsed in parallel.
     *
     * @param rootDirectory root directory that contains POM files
     * @param dependencyKeys dependency keys to resolve (groupId:artifactId)
     * @param logger logger for diagnostic messages
     * @return map of dependency key to resolved MavenCoordinate
     */
    @Override
    public Map<String, MavenCoordinate> buildIndex(Path rootDirectory,
                                                   Set<String> dependencyKeys,
                                                   Logger logger) {
        if (dependencyKeys == null || dependencyKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Path> pomPaths = pomFilesCollector.collectPomFiles(rootDirectory, logger);
        if (pomPaths.isEmpty()) {
            logger.lifecycle("No POM files found under {}", rootDirectory);
            return Collections.emptyMap();
        }

        Set<String> keys = new HashSet<>(dependencyKeys);

        Map<String, List<MavenCoordinate>> grouped = pomPaths
                .parallelStream()
                .map(path -> pomParser.parsePom(path, logger))
                .filter(Objects::nonNull)
                .filter(MavenCoordinate::isValid)
                .filter(coord -> keys.contains(toKey(coord)))
                .collect(Collectors.groupingBy(DefaultPomIndex::toKey));

        Map<String, MavenCoordinate> result = new HashMap<>();
        for (Map.Entry<String, List<MavenCoordinate>> entry : grouped.entrySet()) {
            String key = entry.getKey();
            MavenCoordinate best = chooseBestVersion(entry.getValue(), logger);
            if (best != null) {
                result.put(key, best);
            }
        }

        return result;
    }

    /**
     * Builds a dependency key in the form "groupId:artifactId".
     *
     * @param coord MavenCoordinate
     * @return dependency key string
     */
    private static String toKey(MavenCoordinate coord) {
        return coord.getGroupId() + ":" + coord.getArtifactId();
    }

    /**
     * Chooses the best version from a list of coordinates.
     * <p>
     * Uses Gradle VersionNumber to compare version strings.
     *
     * @param candidates list of coordinates for the same groupId:artifactId
     * @param logger logger for diagnostic messages
     * @return coordinate with the highest version or null if list is empty
     */
    private MavenCoordinate chooseBestVersion(List<MavenCoordinate> candidates, Logger logger) {
        return candidates.stream()
                .filter(c -> c.getVersion() != null)
                .max(Comparator.comparing(c -> parseVersion(c.getVersion(), logger)))
                .orElse(null);
    }

    /**
     * Parses a version string to VersionNumber.
     * <p>
     * Returns VersionNumber.UNKNOWN on failure.
     *
     * @param version version string
     * @param logger logger for diagnostic messages
     * @return parsed VersionNumber
     */
    private VersionNumber parseVersion(String version, Logger logger) {
        try {
            return VersionNumber.parse(version);
        } catch (Exception e) {
            logger.debug("Failed to parse version '{}': {}", version, e.getMessage());
            return VersionNumber.UNKNOWN;
        }
    }
}
