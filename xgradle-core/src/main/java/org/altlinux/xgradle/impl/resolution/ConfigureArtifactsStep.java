package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.configurators.ArtifactConfigurator;

@Singleton
final class ConfigureArtifactsStep implements ResolutionStep {

    private final ArtifactConfigurator artifactConfigurator;

    @Inject
    ConfigureArtifactsStep(ArtifactConfigurator artifactConfigurator) {
        this.artifactConfigurator = artifactConfigurator;
    }

    @Override
    public String name() {
        return "configure-artifacts";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        resolutionContext.gradle.allprojects(p -> artifactConfigurator.configure(
                resolutionContext.gradle,
                resolutionContext.systemArtifacts,
                resolutionContext.dependencyConfigNames
        ));
    }
}
