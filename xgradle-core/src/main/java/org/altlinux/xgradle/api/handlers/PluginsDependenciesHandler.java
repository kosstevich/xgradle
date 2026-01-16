package org.altlinux.xgradle.api.handlers;

import org.gradle.api.initialization.Settings;

/**
 * Handler for configuring plugin resolution and repositories
 * at the settings level.
 */
public interface PluginsDependenciesHandler extends Handler<Settings> {

    /**
     * Configures plugin repositories and resolution strategy.
     *
     * @param settings Gradle settings instance
     */
    @Override
    void handle(Settings settings);
}
