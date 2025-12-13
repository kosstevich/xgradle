package org.altlinux.xgradle.api.processors;

import org.altlinux.xgradle.api.services.PomFinder;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processor for BOM (Bill of Materials) dependencies.
 */
public interface BomProcessor {

    /**
     * Processes project dependencies and extracts BOM-managed dependencies.
     *
     * @param projectDependencies set of dependency keys ("groupId:artifactId")
     * @param pomFinder service for locating BOM POM files
     * @param logger Gradle logger
     *
     * @return full set of dependencies including those from BOMs
     */
    Set<String> process(Set<String> projectDependencies, PomFinder pomFinder, Logger logger);

    /**
     * Removes BOM dependencies from Gradle configurations so that
     * only managed dependencies remain attached.
     *
     * @param gradle current Gradle instance
     */
    void removeBomsFromConfigurations(Gradle gradle);

    /**
     * @return BOM -> list of managed dependency coordinates ("groupId:artifactId:version")
     */
    Map<String, List<String>> getBomManagedDeps();

    /**
     * @return mapping dependencyKey -> managed version
     */
    Map<String, String> getManagedVersions();
}
