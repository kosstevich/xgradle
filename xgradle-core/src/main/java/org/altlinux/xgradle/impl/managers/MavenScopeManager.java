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
package org.altlinux.xgradle.impl.managers;

import com.google.inject.Singleton;
import org.altlinux.xgradle.api.managers.ScopeManager;
import org.altlinux.xgradle.impl.enums.MavenScope;

import java.util.EnumMap;
import java.util.HashMap;

import java.util.Map;

/**
 * Default implementation of ScopeManager that uses MavenScope enum
 * and priority-based updates.
 * <p>
 * Scope priorities (highest to lowest):
 * - COMPILE
 * - RUNTIME
 * - PROVIDED
 * - TEST
 *
 * @author Ivan Khanas
 */
@Singleton
class MavenScopeManager implements ScopeManager {

    private final Map<String, MavenScope> dependencyScopes = new HashMap<>();

    private static final EnumMap<MavenScope, Integer> PRIORITY = new EnumMap<>(MavenScope.class);

    static {
        PRIORITY.put(MavenScope.COMPILE, 0);
        PRIORITY.put(MavenScope.RUNTIME, 1);
        PRIORITY.put(MavenScope.PROVIDED, 2);
        PRIORITY.put(MavenScope.TEST, 3);
    }

    /**
     * Updates the scope of a dependency if the new scope has higher priority.
     * <p>
     * If there is no existing scope for the dependency, the new scope is always applied.
     *
     * @param dependencyKey dependency identifier, usually "groupId:artifactId"
     * @param newScope new scope to apply
     */
    @Override
    public void updateScope(String dependencyKey, MavenScope newScope) {
        if (newScope == null) {
            return;
        }

        MavenScope currentScope = dependencyScopes.get(dependencyKey);
        if (currentScope == null || hasHigherPriority(newScope, currentScope)) {
            dependencyScopes.put(dependencyKey, newScope);
        }
    }

    /**
     * Returns the effective scope for the given dependency.
     * <p>
     * If no scope has been recorded, COMPILE is returned as default.
     *
     * @param dependencyKey dependency identifier
     * @return effective scope, never null
     */
    @Override
    public MavenScope getScope(String dependencyKey) {
        return (dependencyScopes.getOrDefault(dependencyKey, MavenScope.COMPILE));
    }

    /**
     * Checks if newScope has higher priority than currentScope.
     *
     * @param newScope new scope candidate
     * @param currentScope existing scope value
     * @return true if newScope should override currentScope
     */
    private boolean hasHigherPriority(MavenScope newScope, MavenScope currentScope) {
        Integer newPriority = PRIORITY.get(newScope);
        Integer currentPriority = PRIORITY.get(currentScope);


        if (newPriority == null) {
            return false;
        }
        if (currentPriority == null) {
            return true;
        }
        return newPriority < currentPriority;
    }
}
