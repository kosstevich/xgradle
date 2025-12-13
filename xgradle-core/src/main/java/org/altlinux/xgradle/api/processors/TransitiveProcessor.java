package org.altlinux.xgradle.api.processors;

import org.altlinux.xgradle.api.managers.ScopeManager;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import java.util.Map;
import java.util.Set;

/**
 * Processor that categorizes dependencies into main/test scopes
 * based on transitive dependency graph.
 */
public interface TransitiveProcessor {

    /**
     * Processes transitive dependencies and categorizes them into main/test contexts.
     *
     * @param systemArtifacts map of system artifacts (key: "groupId:artifactId")
     */
    void process(Map<String, MavenCoordinate> systemArtifacts);

    /**
     * @return dependencies belonging to main context
     */
    Set<String> getMainDependencies();

    /**
     * @return dependencies belonging to test context
     */
    Set<String> getTestDependencies();

    /**
     * @return effective scope manager used during processing
     */
    ScopeManager getScopeManager();

    /**
     * @return skipped dependencies reported by underlying manager
     */
    Set<String> getSkippedDependencies();
}
