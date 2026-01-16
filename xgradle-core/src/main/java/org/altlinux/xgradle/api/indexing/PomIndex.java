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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PomIndex {

    void build(Path rootDirectory);

    Optional<MavenCoordinate> find(String groupId, String artifactId);

    List<MavenCoordinate> findAllForGroup(String groupId);

    Map<String, MavenCoordinate> snapshot();
}
