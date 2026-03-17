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
package org.altlinux.xgradle.impl.resolution;

import org.altlinux.xgradle.impl.model.MavenCoordinate;
import org.altlinux.xgradle.interfaces.processors.TransitiveProcessor;
import org.altlinux.xgradle.interfaces.processors.TransitiveResult;
import org.altlinux.xgradle.interfaces.services.VersionScanner;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

/**
 * @author Ivan Khanas xeno@altlinux.org
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResolveTransitivesAndScanMissingStep")
class ResolveTransitivesAndScanMissingStepTests {

    @Mock
    private TransitiveProcessor transitiveProcessor;

    @Mock
    private VersionScanner versionScanner;

    @Mock
    private Gradle gradle;

    @Mock
    private Project rootProject;

    @Mock
    private Logger logger;

    @Test
    @DisplayName("Replaces stale artifacts with rescan result")
    void replacesStaleArtifactsWithRescanResult() {
        when(gradle.getRootProject()).thenReturn(rootProject);
        when(rootProject.getLogger()).thenReturn(logger);

        when(transitiveProcessor.process(anyMap(), anySet(), anyMap(), anyMap()))
                .thenReturn(new TransitiveResult(
                        Set.of("org.example:keep", "io.github.toolfactory:narcissus"),
                        Set.of(),
                        Set.of("io.github.toolfactory:narcissus")
                ));

        when(versionScanner.scanSystemArtifacts(
                Set.of("org.example:keep", "io.github.toolfactory:narcissus")
        )).thenReturn(Map.of("org.example:keep", coordinate("org.example", "keep", "2.0.0")));
        when(versionScanner.scanSystemArtifacts(Set.of())).thenReturn(Map.of());

        ResolutionContext context = new ResolutionContext(gradle);
        context.putSystemArtifact("org.example:keep", coordinate("org.example", "keep", "1.0.0"));
        context.putSystemArtifact(
                "io.github.toolfactory:narcissus",
                coordinate("io.github.toolfactory", "narcissus", "1.0.11")
        );

        ResolveTransitivesAndScanMissingStep step =
                new ResolveTransitivesAndScanMissingStep(transitiveProcessor, versionScanner);
        step.execute(context);

        assertEquals(1, context.getSystemArtifacts().size());
        assertTrue(context.getSystemArtifacts().containsKey("org.example:keep"));
        assertFalse(context.getSystemArtifacts().containsKey("io.github.toolfactory:narcissus"));
        assertEquals("2.0.0", context.getSystemArtifacts().get("org.example:keep").getVersion());
        assertTrue(context.getSkipped().contains("io.github.toolfactory:narcissus"));
    }

    private MavenCoordinate coordinate(String groupId, String artifactId, String version) {
        return MavenCoordinate.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .build();
    }
}
