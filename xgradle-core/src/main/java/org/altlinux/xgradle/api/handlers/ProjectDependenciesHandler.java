package org.altlinux.xgradle.api.handlers;

import org.gradle.api.invocation.Gradle;

/**
 * Handler for configuring project dependencies after Gradle configuration phase.
 */
@FunctionalInterface
public interface ProjectDependenciesHandler {

    /**
     * Performs full dependency-resolution pipeline after project configuration:
     *  - collects project dependencies
     *  - processes BOMs
     *  - resolves system artifacts
     *  - processes transitive dependencies
     *  - applies dependency substitutions
     *  - configures artifacts into configurations
     *
     * @param gradle current Gradle instance
     */
    void handleAfterConfiguration(Gradle gradle);
}
