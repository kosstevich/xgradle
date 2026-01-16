package org.altlinux.xgradle.api.collectors;

import org.gradle.api.invocation.Gradle;

import java.util.Map;
import java.util.Set;

/**
 * Collects and aggregates dependency information from all projects in a Gradle build.
 */
public interface DependencyCollector extends Collector<Gradle, Set<String>> {

    @Override
    Set<String> collect(Gradle gradle);

    Map<String, Set<String>> getRequestedVersions();
}
