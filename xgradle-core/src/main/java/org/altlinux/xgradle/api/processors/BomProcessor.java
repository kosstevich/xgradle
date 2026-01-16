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
package org.altlinux.xgradle.api.processors;

import org.gradle.api.invocation.Gradle;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processor for BOM (Bill of Materials) dependencies.
 */
public interface BomProcessor extends Processor<BomProcessor.Context> {


    final class Context {
        private final Gradle gradle;
        private final Set<String> projectDependencies;

        public Context(Gradle gradle, Set<String> projectDependencies) {
            this.gradle = gradle;
            this.projectDependencies = projectDependencies;
        }

        public Gradle getGradle() { return gradle; }
        public Set<String> getProjectDependencies() { return projectDependencies; }
    }

    /**
     * Main entrypoint for BOM processing.
     */
    void process(Context context);

    /**
     * Removes BOM dependencies from Gradle configurations so that
     * only managed dependencies remain attached.
     *
     * @param gradle current Gradle instance
     */
    void removeBomsFromConfigurations(Gradle gradle);

    /**
     * @return BOM -> list of managed dependency coordinates ("groupId:artifactId:version")
     */
    Map<String, List<String>> getBomManagedDeps();

    /**
     * @return mapping dependencyKey -> managed version
     */
    Map<String, String> getManagedVersions();
}
