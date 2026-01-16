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
import org.altlinux.xgradle.api.indexing.PomIndex;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.logging.Logger;
import org.gradle.util.internal.VersionNumber;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
class DefaultPomIndex implements PomIndex {

    private final PomFilesCollector pomFilesCollector;
    private final PomParser pomParser;
    private final Logger logger;

    private volatile Map<String, MavenCoordinate> latestByGa = Collections.emptyMap();
    private volatile Map<String, List<MavenCoordinate>> byGroup = Collections.emptyMap();

    @Inject
    DefaultPomIndex(PomFilesCollector pomFilesCollector, PomParser pomParser, Logger logger) {
        this.pomFilesCollector = pomFilesCollector;
        this.pomParser = pomParser;
        this.logger = logger;
    }

    @Override
    public void build(Path rootDirectory) {
        if (rootDirectory == null || !Files.isDirectory(rootDirectory)) {
            logger.lifecycle("POM root directory is not valid: {}", rootDirectory);
            latestByGa = Collections.emptyMap();
            byGroup = Collections.emptyMap();
            return;
        }

        List<Path> pomFiles = pomFilesCollector.collect(rootDirectory);

        Map<String, MavenCoordinate> latest = new HashMap<>();
        Map<String, List<MavenCoordinate>> groups = new HashMap<>();

        for (Path pomPath : pomFiles) {
            MavenCoordinate coord;
            try {
                coord = pomParser.parsePom(pomPath);
            } catch (RuntimeException e) {
                logger.lifecycle("Failed to parse POM {}: {}", pomPath, e.getMessage());
                continue;
            }

            if (coord == null || coord.getGroupId() == null || coord.getArtifactId() == null) {
                continue;
            }

            if (coord.getPomPath() == null) {
                coord.setPomPath(pomPath);
            }

            groups.computeIfAbsent(coord.getGroupId(), k -> new ArrayList<>()).add(coord);

            String ga = coord.getGroupId() + ":" + coord.getArtifactId();
            latest.compute(ga, (k, prev) -> chooseLatest(prev, coord));
        }

        Map<String, List<MavenCoordinate>> frozenGroups = new HashMap<>();
        for (Map.Entry<String, List<MavenCoordinate>> e : groups.entrySet()) {
            frozenGroups.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
        }

        latestByGa = Collections.unmodifiableMap(latest);
        byGroup = Collections.unmodifiableMap(frozenGroups);
    }

    @Override
    public Optional<MavenCoordinate> find(String groupId, String artifactId) {
        if (groupId == null || artifactId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(latestByGa.get(groupId + ":" + artifactId));
    }

    @Override
    public List<MavenCoordinate> findAllForGroup(String groupId) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        return byGroup.getOrDefault(groupId, Collections.emptyList());
    }

    @Override
    public Map<String, MavenCoordinate> snapshot() {
        return latestByGa;
    }

    private MavenCoordinate chooseLatest(MavenCoordinate a, MavenCoordinate b) {
        if (a == null) return b;
        if (b == null) return a;

        String av = a.getVersion();
        String bv = b.getVersion();

        if (av == null || av.isBlank()) return b;
        if (bv == null || bv.isBlank()) return a;

        VersionNumber an = tryParse(av);
        VersionNumber bn = tryParse(bv);

        if (an != null && bn != null) {
            return an.compareTo(bn) >= 0 ? a : b;
        }

        return av.compareTo(bv) >= 0 ? a : b;
    }

    private VersionNumber tryParse(String v) {
        try {
            return VersionNumber.parse(v);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
