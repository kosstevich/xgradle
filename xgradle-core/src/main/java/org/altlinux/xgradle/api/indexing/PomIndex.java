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
package org.altlinux.xgradle.api.indexing;

import org.altlinux.xgradle.impl.model.MavenCoordinate;
import org.gradle.api.logging.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Index for POM files in a repository.
 * <p>
 * Builds an in-memory index of Maven coordinates
 * for a given repository root and set of dependencies.
 */
public interface PomIndex {

    /**
     * Builds an index of POM files for the given dependency keys.
     * <p>
     * Dependency keys must have the form "groupId:artifactId".
     *
     * @param rootDirectory root directory that contains POM files
     * @param dependencyKeys dependency keys that should be resolved
     * @param logger logger for diagnostic messages
     * @return map of dependency key to resolved MavenCoordinate
     */
    Map<String, MavenCoordinate> buildIndex(Path rootDirectory,
                                            Set<String> dependencyKeys,
                                            Logger logger);
}
