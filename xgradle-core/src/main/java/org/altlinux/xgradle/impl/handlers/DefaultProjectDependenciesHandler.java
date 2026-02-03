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
package org.altlinux.xgradle.impl.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.handlers.ProjectDependenciesHandler;
import org.altlinux.xgradle.api.resolution.SystemDependencyResolution;

import org.gradle.api.invocation.Gradle;

/**
 * Handles dependency resolution and substitution for a Gradle project using system-installed artifacts.
 *
 * <p>This handler adds the system repository to all projects and then delegates the full
 * dependency resolution process (BOM processing, system artifact resolution, transitive closure,
 * configuration, and substitution) to an internal resolution workflow.</p>
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class DefaultProjectDependenciesHandler implements ProjectDependenciesHandler {

    private final SystemDependencyResolution resolution;

    @Inject
    DefaultProjectDependenciesHandler(
            SystemDependencyResolution resolution
    ) {
        this.resolution = resolution;
    }

    public void handle(Gradle gradle) {
        resolution.run(gradle);
    }
}
