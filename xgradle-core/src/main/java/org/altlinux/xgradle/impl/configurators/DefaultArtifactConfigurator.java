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
package org.altlinux.xgradle.impl.configurators;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.configurators.ArtifactConfigurator;
import org.altlinux.xgradle.api.managers.ScopeManager;
import org.altlinux.xgradle.impl.enums.ConfigurationType;
import org.altlinux.xgradle.impl.enums.MavenPackaging;
import org.altlinux.xgradle.impl.enums.MavenScope;
import org.altlinux.xgradle.impl.model.ConfigurationInfo;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.invocation.Gradle;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Singleton
class DefaultArtifactConfigurator implements ArtifactConfigurator {
    private final ScopeManager mavenScopeManager;
    private final Map<String, Set<String>> configurationArtifacts = new HashMap<>();
    private final Map<String, Set<ConfigurationInfo>> dependencyConfigurations;
    private final Set<String> testContextDependencies;

    @Inject
    DefaultArtifactConfigurator(
            ScopeManager mavenScopeManager,
            Map<String, Set<ConfigurationInfo>> dependencyConfigurations,
            Set<String> testContextDependencies) {
        this.mavenScopeManager = mavenScopeManager;
        this.dependencyConfigurations = dependencyConfigurations;
        this.testContextDependencies = testContextDependencies;
    }

    @Override
    public void configure(Gradle gradle,
                          Map<String, MavenCoordinate> systemArtifacts,
                          Map<String, Set<String>> dependencyConfigNames) {

        gradle.allprojects(proj -> {
            configurationArtifacts.clear();
            systemArtifacts.forEach((key, coord) -> {
                if (shouldSkip(coord) || isSelfDependency(proj, coord)) return;

                Set<String> configNames = dependencyConfigNames.get(key);
                if (configNames != null && !configNames.isEmpty()) {
                    addToOriginalConfigurations(proj, key, coord, configNames);
                } else {
                    addBasedOnScope(proj, key, coord);
                }
            });
        });
    }

    private boolean shouldSkip(MavenCoordinate coord) {
        return coord.isBom() || MavenPackaging.POM.getPackaging().equals(coord.getPackaging());
    }

    private void addToOriginalConfigurations(Project project, String key,
                                             MavenCoordinate coord,
                                             Set<String> configNames) {
        if (isSelfDependency(project, coord)) {
            return;
        }

        String notation = key + ":" + coord.getVersion();
        for (String configName : configNames) {
            Configuration config = project.getConfigurations().findByName(configName);
            if (config != null && canModifyConfiguration(config)) {
                try {
                    project.getDependencies().add(configName, notation);
                    trackArtifact(configName, notation);
                } catch (Exception e) {
                    project.getLogger().debug("Cannot modify configuration '{}': {}", configName, e.getMessage());
                }
            }
        }
    }

    private void addBasedOnScope(Project project, String key, MavenCoordinate coord) {
        if (isSelfDependency(project, coord)) {
            return;
        }

        String notation = key + ":" + coord.getVersion();

        if (testContextDependencies.contains(key) || coord.isTestContext()) {
            safeAddToConfiguration(project, ConfigurationType.TEST.gradleConfiguration(), notation);
            return;
        }

        ConfigurationType type = determineConfigurationType(key);

        if (type != null && type != ConfigurationType.UNKNOWN) {
            safeAddToConfiguration(project, type.gradleConfiguration(), notation);
            return;
        }

        addBasedOnScopeDefault(project, key, notation);
    }

    private ConfigurationType determineConfigurationType(String key) {
        Set<ConfigurationInfo> infos = dependencyConfigurations.get(key);
        if (infos == null || infos.isEmpty()) {
            return null;
        }

        for (ConfigurationInfo info : infos) {
            if (info.hasTestConfiguration()) {
                continue;
            }

            ConfigurationType t = info.getType();
            if (t != null && t != ConfigurationType.UNKNOWN) {
                return t;
            }
        }

        return null;
    }

    private void addBasedOnScopeDefault(Project project, String key, String notation) {
        MavenScope scope = mavenScopeManager.getScope(key);

        if (scope == MavenScope.PROVIDED || scope == MavenScope.COMPILE) {
            safeAddToConfiguration(project, ConfigurationType.COMPILE_ONLY.gradleConfiguration(), notation);
            return;
        }

        if (scope == MavenScope.RUNTIME) {
            safeAddToConfiguration(project, ConfigurationType.RUNTIME.gradleConfiguration(), notation);
            return;
        }

        if (scope == MavenScope.TEST) {
            safeAddToConfiguration(project, ConfigurationType.TEST.gradleConfiguration(), notation);
            return;
        }

        safeAddToConfiguration(project, ConfigurationType.IMPLEMENTATION.gradleConfiguration(), notation);
    }

    private void trackArtifact(String config, String artifact) {
        configurationArtifacts
                .computeIfAbsent(config, k -> new LinkedHashSet<>())
                .add(artifact);
    }

    private boolean isSelfDependency(Project project, MavenCoordinate coord) {
        if (coord.getGroupId() == null || coord.getArtifactId() == null) {
            return false;
        }

        Map<String, Project> projectIndex = new HashMap<>();
        Project root = project.getRootProject();
        for (Project p : root.getAllprojects()) {
            Object group = p.getGroup();
            String name = p.getName();
            if (group != null && name != null) {
                String k = group + ":" + name;
                projectIndex.put(k, p);
            }
        }

        String coordKey = coord.getGroupId() + ":" + coord.getArtifactId();
        boolean isSelf = projectIndex.containsKey(coordKey);

        if (isSelf) {
            project.getLogger().debug("Detected project dependency, skipping: {}:{}:{}",
                    coord.getGroupId(), coord.getArtifactId(), coord.getVersion());
        }

        return isSelf;
    }

    private void safeAddToConfiguration(Project project, String configName, String notation) {
        Configuration config = project.getConfigurations().findByName(configName);
        if (config != null && canModifyConfiguration(config)) {
            try {
                project.getDependencies().add(configName, notation);
                trackArtifact(configName, notation);
            } catch (Exception e) {
                project.getLogger().debug("Cannot add to configuration '{}': {}", configName, e.getMessage());
            }
        }
    }

    private boolean canModifyConfiguration(Configuration configuration) {
        return configuration.getState() != Configuration.State.RESOLVED;
    }

    public Map<String, Set<String>> getConfigurationArtifacts() {
        return configurationArtifacts;
    }
}
