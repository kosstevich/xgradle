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
package org.altlinux.xgradle.impl.managers;

import com.google.inject.Inject;

import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.altlinux.xgradle.api.managers.ScopeManager;
import org.altlinux.xgradle.api.managers.TransitiveDependencyManager;
import org.altlinux.xgradle.api.maven.PomFinder;

import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.enums.MavenScope;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.logging.Logger;
import java.util.*;

import static org.altlinux.xgradle.impl.utils.ui.Painter.green;

/**
 * Manages transitive dependency resolution in Gradle projects.
 * Performs breadth-first traversal of dependency trees while handling
 * scope filtering and version placeholder resolution.
 *
 * @author Ivan Khanas
 */
@Singleton
class DefaultTransitiveDependencyManager implements TransitiveDependencyManager {
    private final PomFinder pomFinder;
    private final PomParser pomParser;
    private final Logger logger;

    private final ScopeManager mavenScopeManager;
    private final Set<String> processedArtifacts = new HashSet<>();
    private final Set<String> skippedDependencies = new HashSet<>();

    /**
     * Creates a new BOM manager with required services.
     *
     * @param pomFinder service for locating POM files
     * @param logger Gradle logger instance
     */
    @Inject
    DefaultTransitiveDependencyManager(
            PomFinder pomFinder,
            PomParser pomParser,
            ScopeManager mavenScopeManager,
            Logger logger
            ) {
        this.pomFinder = pomFinder;
        this.pomParser = pomParser;
        this.mavenScopeManager = mavenScopeManager;
        this.logger = logger;
    }

    /**
     * Processes transitive dependencies using BFS traversal:
     * - Updates dependency scopes
     * - Resolves new dependencies
     * - Propagates test context flags
     * - Tracks skipped dependencies
     *
     * <p>Modifies the input map by adding resolved dependencies.
     *
     * @param systemArtifacts Map of initial artifacts (will be modified)
     */
    @Override
    public void configure(Map<String, MavenCoordinate> systemArtifacts) {
        logger.lifecycle(green(">>> Processing transitive dependencies"));
        Queue<MavenCoordinate> queue = new LinkedList<>(systemArtifacts.values());

        while (!queue.isEmpty()) {
            MavenCoordinate current = queue.poll();
            if (current.getPomPath() == null) continue;

            List<MavenCoordinate> dependencies = pomParser
                    .parseDependencies(current.getPomPath());

            for (MavenCoordinate dep : dependencies) {
                String depKey = dep.getGroupId() + ":" + dep.getArtifactId();
                mavenScopeManager.updateScope(depKey, dep.getScope());
                if (MavenScope.TEST.equals(dep.getScope())) continue;

                MavenCoordinate resolvedDep = systemArtifacts.get(depKey);
                if (resolvedDep == null) {
                    resolvedDep = pomFinder.findPomForArtifact(
                            dep.getGroupId(), dep.getArtifactId());
                    if (resolvedDep == null) {
                        logger.warn("Skipping not found dependency: {}", depKey);
                        skippedDependencies.add(depKey);
                        continue;
                    }
                    systemArtifacts.put(depKey, resolvedDep);
                }

                resolvedDep.setTestContext(current.isTestContext());

                if (!processedArtifacts.contains(depKey)) {
                    processedArtifacts.add(depKey);
                    queue.add(resolvedDep);
                }
            }
        }
    }

    /**
     * Gets dependencies skipped during processing:
     * - Test-scoped dependencies
     * - Dependencies not found in repositories
     *
     * @return Set of skipped dependency IDs ("groupId:artifactId")
     */
    @Override
    public Set<String> getSkippedDependencies() {
        return skippedDependencies;
    }
}