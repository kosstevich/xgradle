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
package org.altlinux.xgradle.impl.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.altlinux.xgradle.api.handlers.PluginsDependenciesHandler;
import org.altlinux.xgradle.api.handlers.ProjectDependenciesHandler;
import org.altlinux.xgradle.impl.di.XGradlePluginModule;

import org.altlinux.xgradle.impl.utils.ui.LogoPrinter;

import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;

import org.jetbrains.annotations.NotNull;

/**
 * Class implements {@link Plugin} interface
 * <p>Core plugin implementation that applies to Gradle itself rather than individual projects.
 *
 * <p>This plugin provides:
 * <ul>
 *   <li>Custom dependency resolution for other Gradle plugins</li>
 *   <li>Access to all phases of the Gradle build lifecycle</li>
 * </ul>
 *
 * <p>Lifecycle hooks:
 * <ol>
 *   <li>{@code beforeSettings}: Handles plugin dependencies resolution</li>
 *   <li>{@code projectsLoaded}: Adds custom repository configuration</li>
 *   <li>{@code projectsEvaluated}: Processes project dependencies after configuration</li>
 * </ol>
 *
 * @author Ivan Khanas
 */
public final class XGradlePlugin implements Plugin<Gradle> {

    @Override
    public void apply(@NotNull Gradle gradle) {
        if (LogoPrinter.isLogoEnabled()) {
            LogoPrinter.printCenteredBanner();
        }

        Injector injector = Guice.createInjector(
                new XGradlePluginModule()
        );

        PluginsDependenciesHandler plugins = injector.getInstance(PluginsDependenciesHandler.class);
        ProjectDependenciesHandler dependencies = injector.getInstance(ProjectDependenciesHandler.class);

        gradle.beforeSettings(plugins::handle);
        gradle.projectsEvaluated(dependencies::handle);
    }
}
