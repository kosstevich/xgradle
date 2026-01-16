package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.resolvers.ArtifactResolver;

@Singleton
final class ResolveSystemArtifactsStep implements ResolutionStep {

    private final ArtifactResolver artifactResolver;

    @Inject
    ResolveSystemArtifactsStep(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    @Override
    public String name() {
        return "resolve-system-artifacts";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        artifactResolver.resolve(resolutionContext.allDeps, resolutionContext.gradle.getRootProject().getLogger());
        artifactResolver.filter();
        resolutionContext.systemArtifacts = artifactResolver.getSystemArtifacts();
        resolutionContext.notFound = artifactResolver.getNotFoundDependencies();
    }
}
