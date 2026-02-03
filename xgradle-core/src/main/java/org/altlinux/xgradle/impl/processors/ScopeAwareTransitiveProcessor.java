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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Applies test-context markers to root artifacts and delegates transitive traversal to
 * TransitiveDependencyManager. After traversal, categorizes all discovered artifacts into
 * main and test sets and exposes skipped dependencies reported by the manager.
 *
 * The processor is stateful only for the duration of a single process call:
 * main/test sets are cleared at the beginning of each execution.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class ScopeAwareTransitiveProcessor implements TransitiveProcessor {

    private final TransitiveDependencyManager transitiveManager;

    private final Set<String> mainDependencies = new HashSet<>();
    private final Set<String> testDependencies = new HashSet<>();

    private Set<String> testContextDependencies = Collections.emptySet();

    @Inject
    ScopeAwareTransitiveProcessor(TransitiveDependencyManager transitiveManager) {
        this.transitiveManager = transitiveManager;
    }

    @Override
    public void setTestContextDependencies(Set<String> testContextDependencies) {
        this.testContextDependencies = (testContextDependencies == null)
                ? Collections.emptySet()
                : testContextDependencies;
    }

    @Override
    public void process(Map<String, MavenCoordinate> systemArtifacts) {
        mainDependencies.clear();
        testDependencies.clear();

        if (systemArtifacts == null || systemArtifacts.isEmpty()) {
            return;
        }

        for (Map.Entry<String, MavenCoordinate> entry : systemArtifacts.entrySet()) {
            String artifactKey = entry.getKey();
            MavenCoordinate coordinate = entry.getValue();

            if (coordinate == null) {
                continue;
            }

            if (testContextDependencies.contains(artifactKey) && !coordinate.isTestContext()) {
                systemArtifacts.put(
                        artifactKey,
                        coordinate.toBuilder()
                                .testContext(true)
                                .build()
                );
            }
        }

        transitiveManager.configure(systemArtifacts);

        for (Map.Entry<String, MavenCoordinate> entry : systemArtifacts.entrySet()) {
            MavenCoordinate coordinate = entry.getValue();
            if (coordinate != null && coordinate.isTestContext()) {
                testDependencies.add(entry.getKey());
            } else {
                mainDependencies.add(entry.getKey());
            }
        }
    }

    @Override
    public Set<String> getMainDependencies() {
        return mainDependencies;
    }

    @Override
    public Set<String> getTestDependencies() {
        return testDependencies;
    }

    @Override
    public Set<String> getSkippedDependencies() {
        return transitiveManager.getSkippedDependencies();
    }
}
