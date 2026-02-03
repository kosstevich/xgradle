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

import org.altlinux.xgradle.api.maven.PomFinder;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.api.processors.BomProcessor;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Processes BOM (Bill of Materials) dependencies in Gradle projects.
 * <p>
 * This processor handles:
 * <ul>
 *   <li>Identification of BOM dependencies in project configurations</li>
 *   <li>Recursive processing of transitive BOM dependencies</li>
 *   <li>Collection of dependency versions managed through BOMs</li>
 *   <li>Post-processing removal of BOMs from dependency configurations</li>
 * </ul>
 *
 * <p>The core processing occurs in {@link #process(Context)} which can expand
 * the project's dependency set by incorporating BOM-managed dependencies (group:artifact).
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class DefaultBomProcessor implements BomProcessor {

    private final Map<String, List<String>> bomManagedDeps = new LinkedHashMap<>();
    private final Set<String> processedBoms = new HashSet<>();
    private final Map<String, String> managedVersions = new HashMap<>();

    private final PomFinder pomFinder;
    private final PomParser pomParser;
    private final Logger logger;

    @Inject
    DefaultBomProcessor(PomFinder pomFinder, PomParser pomParser, Logger logger) {
        this.pomFinder = pomFinder;
        this.pomParser = pomParser;
        this.logger = logger;
    }

    @Override
    public void process(Context context) {
        bomManagedDeps.clear();
        processedBoms.clear();
        managedVersions.clear();

        if (context == null || context.getProjectDependencies() == null) {
            return;
        }

        Set<String> projectDependencies = context.getProjectDependencies();
        Queue<MavenCoordinate> bomQueue = new LinkedList<>();

        for (String dep : projectDependencies) {
            if (dep == null) {
                continue;
            }

            String[] parts = dep.split(":");
            if (parts.length < 2) {
                continue;
            }

            MavenCoordinate coord = pomFinder.findPomForArtifact(parts[0], parts[1]);
            if (coord != null && coord.isBom()) {
                bomQueue.add(coord);
                String bomId = coord.getGroupId() + ":" + coord.getArtifactId();
                processedBoms.add(bomId);
            }
        }

        while (!bomQueue.isEmpty()) {
            MavenCoordinate bom = bomQueue.poll();

            String bomVersion = bom.getVersion() == null ? "unknown" : bom.getVersion();
            String bomKey = bom.getGroupId() + ":" + bom.getArtifactId() + ":" + bomVersion;

            List<String> managedDeps = new ArrayList<>();
            List<MavenCoordinate> dependencies = pomParser.parseDependencyManagement(bom.getPomPath());

            for (MavenCoordinate dep : dependencies) {
                String depId = dep.getGroupId() + ":" + dep.getArtifactId();

                projectDependencies.add(depId);

                String version = dep.getVersion();
                if (version != null && !version.isBlank()) {
                    managedVersions.put(depId, version);
                    managedDeps.add(depId + ":" + version);
                } else {
                    managedDeps.add(depId);
                }

                if (dep.isBom() && !processedBoms.contains(depId)) {
                    bomQueue.add(dep);
                    processedBoms.add(depId);
                }
            }

            bomManagedDeps.put(bomKey, managedDeps);
        }
    }

    @Override
    public void removeBomsFromConfigurations(Gradle gradle) {
        if (gradle == null) {
            return;
        }

        gradle.allprojects(p -> p.getConfigurations().all(cfg -> {
            List<Dependency> toRemove = new ArrayList<>();

            for (Dependency d : cfg.getDependencies()) {
                if (d.getGroup() == null || d.getName() == null) {
                    continue;
                }
                String key = d.getGroup() + ":" + d.getName();
                if (processedBoms.contains(key)) {
                    toRemove.add(d);
                }
            }

            toRemove.forEach(dep -> cfg.getDependencies().remove(dep));
        }));
    }

    @Override
    public Map<String, List<String>> getBomManagedDeps() {
        return bomManagedDeps;
    }

    @Override
    public Map<String, String> getManagedVersions() {
        return managedVersions;
    }
}
