package org.altlinux.xgradle.api.handlers;

import org.gradle.api.initialization.Settings;

/**
 * Handler for configuring plugin resolution and repositories
 * at the settings level.
 */
@FunctionalInterface
public interface PluginsDependenciesHandler {

    /**
     * Configures plugin repositories and resolution strategy.
     *
     * @param settings Gradle settings instance
     */
    void handle(Settings settings);
}
