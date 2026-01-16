package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.collectors.DependencyCollector;

@Singleton
final class CollectDeclaredDependenciesStep implements ResolutionStep {

    private final DependencyCollector dependencyCollector;

    @Inject
    CollectDeclaredDependenciesStep(DependencyCollector dependencyCollector) {
        this.dependencyCollector = dependencyCollector;
    }

    @Override
    public String name() {
        return "collect-declared-dependencies";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        resolutionContext.projectDeps = dependencyCollector.collect(resolutionContext.gradle);
        resolutionContext.requestedVersions = dependencyCollector.getRequestedVersions();
        resolutionContext.allDeps = resolutionContext.projectDeps;
    }
}
