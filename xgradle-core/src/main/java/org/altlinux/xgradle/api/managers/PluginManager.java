package org.altlinux.xgradle.api.managers;

import org.gradle.api.initialization.Settings;

/**
 * Manager for plugin repositories and plugin resolution pipeline.
 */
public interface PluginManager extends Manager<Settings>{

    /**
     * Configures plugin repositories and resolution for the given settings.
     *
     * @param settings Gradle settings instance
     */
    @Override
    void configure(Settings settings);
}
