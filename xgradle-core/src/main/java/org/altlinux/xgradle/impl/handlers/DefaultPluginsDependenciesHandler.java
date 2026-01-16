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
 * See the License  for the specific language governing permissions and
 * limitations under the License.
 */
package org.altlinux.xgradle.impl.handlers;

import com.google.inject.Inject;

import com.google.inject.Singleton;
import org.altlinux.xgradle.api.handlers.PluginsDependenciesHandler;
import org.altlinux.xgradle.api.managers.PluginManager;

import org.altlinux.xgradle.impl.extensions.SystemDepsExtension;

import org.gradle.api.initialization.Settings;

/**
 * Central handler for managing plugin dependencies in Gradle builds.
 *
 * <p>This class serves as the main entry point for configuring plugin dependency resolution
 * using locally available artifacts. It orchestrates the process of:</p>
 * <ul>
 *   <li>Scanning configured system directories for available plugin artifacts</li>
 *   <li>Adding local directories as flat repository sources for plugin resolution</li>
 *   <li>Intercepting plugin resolution requests to provide specific versions from local artifacts</li>
 *   <li>Handling both regular plugins and BOM (Bill of Materials) based plugin packages</li>
 * </ul>
 *
 * <p>The handler uses a {@link PluginManager} to delegate the actual plugin management operations,
 * providing a clean separation of concerns while ensuring consistent plugin resolution behavior
 * across all projects in the build.</p>
 *
 * <p><strong>Note:</strong> This handler specifically focuses on Gradle plugins and should not
 * be confused with regular project dependency management.</p>
 *
 * @see PluginManager
 * @see SystemDepsExtension#getJarsPath()
 *
 * @author Ivan Khanas
 */
@Singleton
class DefaultPluginsDependenciesHandler implements PluginsDependenciesHandler {

    private final PluginManager pluginManager;

    /**
     * Constructs a new PluginsDependenciesHandler with default service implementations.
     *
     * <p>Initializes the handler with:
     * <ul>
     *   <li>A {@link PluginManager} to handle plugin resolution operations</li>
     * </ul>
     * </p>
     */
    @Inject
    DefaultPluginsDependenciesHandler(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * Initializes and configures plugin dependency resolution for the given Gradle settings.
     *
     * <p>This method:
     * <ol>
     *   <li>Validates the existence of the system jars directory</li>
     *   <li>Adds the directory and its subdirectories as flatDir repositories to plugin management</li>
     *   <li>Configures a plugin resolution strategy to resolve plugins from local artifacts</li>
     *   <li>Handles both regular plugins and BOM-based plugin packages</li>
     * </ol>
     *
     * <p>If the system jars directory does not exist or is not accessible, a warning is logged
     * but the build continues with standard plugin resolution mechanisms.</p>
     *
     * @param settings the Gradle Settings object used to configure plugin management
     */
    public void handle(Settings settings) {
        pluginManager.configure(settings);
    }
}