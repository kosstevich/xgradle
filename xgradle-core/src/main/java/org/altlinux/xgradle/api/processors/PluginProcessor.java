package org.altlinux.xgradle.api.processors;

import org.gradle.api.initialization.Settings;

/**
 * Processor for Gradle plugin resolution.
 */
public interface PluginProcessor {

    /**
     * Configures plugin resolution strategy (eachPlugin hook) for given settings.
     *
     * @param settings Gradle settings instance
     */
    void configurePluginResolution(Settings settings);
}
