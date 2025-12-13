package org.altlinux.xgradle.api.collectors;

import org.altlinux.xgradle.impl.collectors.info.ConfigurationInfo;
import org.gradle.api.invocation.Gradle;

import java.util.Map;
import java.util.Set;

/**
 * Collector for configuration-level dependency metadata in a Gradle build.
 */
public interface ConfigurationInfoCollector {

    /**
     * Collects configuration metadata for all projects in Gradle build.
     *
     * @param gradle current Gradle instance
     */
    void collect(Gradle gradle);

    /**
     * @return mapping dependencyKey -> set of configuration info objects
     */
    Map<String, Set<ConfigurationInfo>> getDependencyConfigurations();

    /**
     * @return mapping dependencyKey -> true if dependency is used in test configurations
     */
    Map<String, Boolean> getTestDependencyFlags();

    /**
     * @return mapping dependencyKey -> set of configuration names
     */
    Map<String, Set<String>> getDependencyConfigNames();
}
