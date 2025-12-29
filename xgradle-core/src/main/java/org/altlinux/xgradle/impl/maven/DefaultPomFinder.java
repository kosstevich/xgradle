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
package org.altlinux.xgradle.impl.maven;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.api.maven.PomFinder;

import org.altlinux.xgradle.impl.extensions.SystemDepsExtension;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Default implementation of {@link PomFinder} that searches for POM files
 * in directories configured via {@link SystemDepsExtension}.
 *
 * <p>This implementation walks a limited directory depth for each configured
 * POM root, parses candidate POMs and validates the resulting coordinates
 * against the requested group and artifact identifiers.</p>
 *
 * @author Ivan Khanas
 */
@Singleton
public class DefaultPomFinder implements PomFinder {

    private static final int MAX_SEARCH_DEPTH = 3;

    /**
     * Root directories where POM files are expected to be located.
     */
    private final List<Path> pomDirs = List.of(Paths.get(SystemDepsExtension.getPomsPath()));

    private final PomParser pomParser;
    private final MavenPomFilenameMatcher filenameMatcher;

    /**
     * Constructs a {@code DefaultPomFinder} with the specified dependencies.
     *
     * @param pomParser       parser implementation used to read POM contents
     * @param filenameMatcher helper for matching POM filenames to artifacts
     */
    @Inject
    DefaultPomFinder(PomParser pomParser, MavenPomFilenameMatcher filenameMatcher) {
        this.pomParser = pomParser;
        this.filenameMatcher = filenameMatcher;
    }

    @Override
    public MavenCoordinate findPomForArtifact(String groupId,
                                              String artifactId,
                                              Logger logger) {
        Set<String> nameVariants = filenameMatcher.generateNameVariants(groupId, artifactId);

        return pomDirs.stream()
                .filter(Files::isDirectory)
                .flatMap(dir -> nameVariants.stream()
                        .flatMap(variant -> walkSafely(dir, logger)
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".pom"))
                                .filter(path -> filenameMatcher.matches(path, variant, artifactId))
                        )
                )
                .map(pomPath -> pomParser.parsePom(pomPath, logger))
                .filter(coord -> isValidCoordinate(coord, groupId, artifactId, logger))
                .findFirst()
                .orElse(null);
    }


    @Override
    public List<MavenCoordinate> findAllPomsForGroup(String groupId, Logger logger) {
        List<MavenCoordinate> candidates = new ArrayList<>();

        for (Path dir : pomDirs) {
            if (!Files.isDirectory(dir)) {
                continue;
            }

            try (var stream = Files.walk(dir, MAX_SEARCH_DEPTH)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".pom"))
                        .forEach(pomPath -> {
                            MavenCoordinate coord = pomParser.parsePom(pomPath, logger);
                            if (isValidGroupCoordinate(coord, groupId)) {
                                candidates.add(coord);
                            }
                        });
            } catch (IOException e) {
                logger.lifecycle("Group POM scan error in {}: {}", dir, e.getMessage());
            }
        }

        return candidates;
    }

    /**
     * Validates that a parsed Maven coordinate matches expected values
     * and is structurally valid.
     *
     * @param coord      parsed Maven coordinate to validate
     * @param groupId    expected group ID
     * @param artifactId expected artifact ID
     * @param logger     logger instance for reporting validation problems
     *
     * @return {@code true} if the coordinate is valid and matches the
     *         requested identifiers, {@code false} otherwise
     */
    private boolean isValidCoordinate(MavenCoordinate coord,
                                      String groupId,
                                      String artifactId,
                                      Logger logger) {
        if (coord == null) {
            return false;
        }

        if (!coord.isValid()) {
            logger.lifecycle("Invalid coordinate in POM: {}", coord);
            return false;
        }

        if (!groupId.equals(coord.getGroupId())) {
            logger.lifecycle("Group ID mismatch: expected {}, found {}",
                    groupId, coord.getGroupId());
            return false;
        }

        if (!artifactId.equals(coord.getArtifactId())) {
            logger.lifecycle("Artifact ID mismatch: expected {}, found {}",
                    artifactId, coord.getArtifactId());
            return false;
        }

        return true;
    }

    /**
     * Walks the given directory safely and returns a materialised stream of paths.
     *
     * <p>The underlying {@link java.nio.file.Files#walk(Path, FileVisitOption...)} walk(Path, int)} stream is fully
     * consumed and closed inside this method to avoid resource leaks.</p>
     *
     * @param dir    root directory to walk
     * @param logger logger instance for reporting IO errors
     * @return stream of discovered paths or an empty stream if an error occurs
     */
    private Stream<Path> walkSafely(Path dir, Logger logger) {
        try (Stream<Path> stream = Files.walk(dir, DefaultPomFinder.MAX_SEARCH_DEPTH)) {
            return stream;
        } catch (IOException e) {
            logger.lifecycle("POM search error in {}: {}", dir, e.getMessage());
            return Stream.empty();
        }
    }


    /**
     * Validates that a parsed Maven coordinate belongs to a given group.
     *
     * @param coord   parsed Maven coordinate to validate
     * @param groupId expected group ID
     *
     * @return {@code true} if the coordinate is valid and belongs to the group,
     *         {@code false} otherwise
     */
    private boolean isValidGroupCoordinate(MavenCoordinate coord, String groupId) {
        return coord != null
                && coord.isValid()
                && groupId.equals(coord.getGroupId());
    }
}
