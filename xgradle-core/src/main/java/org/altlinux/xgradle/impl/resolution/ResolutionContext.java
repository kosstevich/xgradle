package org.altlinux.xgradle.impl.resolution;

import org.altlinux.xgradle.api.indexing.PomIndex;
import org.altlinux.xgradle.impl.model.ConfigurationInfo;
import org.altlinux.xgradle.impl.model.MavenCoordinate;
import org.altlinux.xgradle.impl.resolvers.DependencySubstitutor;
import org.gradle.api.invocation.Gradle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ResolutionContext {
    public final Gradle gradle;

    public Set<String> projectDeps = new HashSet<>();
    public Set<String> allDeps = new HashSet<>();
    public Map<String, Set<String>> requestedVersions = new HashMap<>();

    public Map<String, Boolean> testDependencyFlags = new HashMap<>();
    public Map<String, Set<ConfigurationInfo>> dependencyConfigurations = new HashMap<>();
    public Map<String, Set<String>> dependencyConfigNames = new HashMap<>();

    public Set<String> testContextDependencies = new HashSet<>();

    public Map<String, MavenCoordinate> systemArtifacts = new HashMap<>();
    public Set<String> notFound = new HashSet<>();
    public Set<String> skipped = new HashSet<>();

    public DependencySubstitutor substitutor;

    public List<Path> pomFiles = new ArrayList<>();
    public PomIndex pomIndex;

    public ResolutionContext(Gradle gradle) {
        this.gradle = gradle;
    }
}
