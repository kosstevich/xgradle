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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.processors.TransitiveProcessor;
import org.altlinux.xgradle.api.resolution.ResolutionStep;
import org.altlinux.xgradle.api.services.VersionScanner;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Expands dependency set using transitive traversal and ensures that all discovered coordinates are
 * resolved against system libraries via VersionScanner. Test-context is preserved for test artifacts.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class ResolveTransitivesAndScanMissingStep implements ResolutionStep {

    private final TransitiveProcessor transitiveProcessor;
    private final VersionScanner versionScanner;

    @Inject
    ResolveTransitivesAndScanMissingStep(
            TransitiveProcessor transitiveProcessor,
            VersionScanner versionScanner
    ) {
        this.transitiveProcessor = transitiveProcessor;
        this.versionScanner = versionScanner;
    }

    @Override
    public String name() {
        return "resolve-transitive-dependencies";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        transitiveProcessor.setTestContextDependencies(resolutionContext.getTestContextDependencies());
        transitiveProcessor.process(resolutionContext.getSystemArtifacts());

        Set<String> mainDependencyKeys = new HashSet<>(transitiveProcessor.getMainDependencies());
        Set<String> testDependencyKeys = new HashSet<>(transitiveProcessor.getTestDependencies());

        resolutionContext.getTestContextDependencies().addAll(testDependencyKeys);

        resolutionContext.getSkipped().clear();
        resolutionContext.getSkipped().addAll(transitiveProcessor.getSkippedDependencies());

        Map<String, MavenCoordinate> resolvedMain = versionScanner.scanSystemArtifacts(mainDependencyKeys);
        Map<String, MavenCoordinate> resolvedTest = versionScanner.scanSystemArtifacts(testDependencyKeys);

        for (Map.Entry<String, MavenCoordinate> entry : resolvedTest.entrySet()) {
            MavenCoordinate coordinate = entry.getValue();
            if (coordinate == null) {
                continue;
            }

            resolvedTest.put(
                    entry.getKey(),
                    coordinate.toBuilder()
                            .testContext(true)
                            .build()
            );
        }


        resolutionContext.getSystemArtifacts().putAll(resolvedMain);
        resolutionContext.getSystemArtifacts().putAll(resolvedTest);
    }
}
