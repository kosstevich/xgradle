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
package org.altlinux.xgradle.impl.processors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.managers.TransitiveDependencyManager;

import org.altlinux.xgradle.api.processors.TransitiveProcessor;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import java.util.*;

/**
 * Processes transitive dependencies and categorizes them into main and test scopes.
 * <p>
 * This processor:
 * <ul>
 *   <li>Analyzes the dependency tree to identify transitive relationships</li>
 *   <li>Separates dependencies into main and test contexts</li>
 *   <li>Applies scope management rules</li>
 *   <li>Tracks skipped dependencies</li>
 *   <li>Propagates test context flags through the dependency tree</li>
 * </ul>
 *
 * <p>Works in conjunction with {@link TransitiveDependencyManager} to traverse
 * and process the dependency graph.
 *
 * @author Ivan Khanas
 */
@Singleton
class ScopeAwareTransitiveProcessor implements TransitiveProcessor {

    private final TransitiveDependencyManager transitiveManager;

    private final Set<String> mainDependencies = new HashSet<>();
    private final Set<String> testDependencies = new HashSet<>();

    private Set<String> testContextDependencies = Collections.emptySet();

    @Inject
    ScopeAwareTransitiveProcessor(
            TransitiveDependencyManager transitiveManager
    ) {
        this.transitiveManager = transitiveManager;
    }

    /**
     * Processes transitive dependencies for all system artifacts.
     * <p>
     * Execution flow:
     * <ol>
     *   <li>Marks test context dependencies in system artifacts</li>
     *   <li>Traverses dependency graph using breadth-first search</li>
     *   <li>Updates dependency scopes based on hierarchy</li>
     *   <li>Categorizes dependencies into main/test contexts</li>
     *   <li>Collects skipped dependencies</li>
     * </ol>
     *
     * @param systemArtifacts Resolved system artifacts (key: "groupId:artifactId")
     */
    public void process(Map<String, MavenCoordinate> systemArtifacts) {
        for (MavenCoordinate coord : systemArtifacts.values()) {
            if (testContextDependencies.contains(coord.getGroupId() + ":" + coord.getArtifactId())) {
                coord.setTestContext(true);
            }
        }

        transitiveManager.configure(systemArtifacts);

        for (Map.Entry<String, MavenCoordinate> entry : systemArtifacts.entrySet()) {
            MavenCoordinate coord = entry.getValue();
            if (coord.isTestContext()) {
                testDependencies.add(entry.getKey());
            } else {
                mainDependencies.add(entry.getKey());
            }
        }
    }

    /**
     * Sets pre-identified test-scoped dependencies for the next {@link #process} call.
     */
    @Override
    public void setTestContextDependencies(Set<String> testContextDependencies) {
        this.testContextDependencies = (testContextDependencies == null)
                ? Collections.emptySet()
                : testContextDependencies;
    }

    /**
     * Gets main-context dependencies after processing.
     *
     * @return set of dependency identifiers (format: "groupId:artifactId")
     */
    public Set<String> getMainDependencies() {
        return mainDependencies;
    }

    /**
     * Gets test-context dependencies after processing.
     *
     * @return set of dependency identifiers (format: "groupId:artifactId")
     */
    public Set<String> getTestDependencies() {
        return testDependencies;
    }

    /**
     * Gets dependencies skipped during processing.
     *
     * @return set of skipped dependency identifiers (format: "groupId:artifactId")
     */
    public Set<String> getSkippedDependencies() {
        return transitiveManager.getSkippedDependencies();
    }
}