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
package org.altlinux.xgradle.api.caches;

import com.google.common.collect.ImmutableList;

import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.logging.Logger;

import java.util.Map;

public interface PomDataCache {
    MavenCoordinate getPom(String key);

    void putPom(String key, MavenCoordinate coordinate);

    void invalidatePom(String key);

    ImmutableList<MavenCoordinate> getDependencyManagement(String key);

    void putDependencyManagement(String key, ImmutableList<MavenCoordinate> dependencies);

    ImmutableList<MavenCoordinate> getDependencies(String key);

    void putDependencies(String key, ImmutableList<MavenCoordinate> dependencies);

    Map<String, String> getProperties(String key);

    void putProperties(String key, Map<String, String> properties);

    void logStats(Logger logger);
}
