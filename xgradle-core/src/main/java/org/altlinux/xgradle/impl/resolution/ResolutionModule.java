package org.altlinux.xgradle.impl.resolution;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.util.List;

public class ResolutionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SystemDependencyResolution.class).to(DefaultSystemDependencyResolution.class);
        bind(ResolutionReporter.class).to(DefaultResolutionReporter.class);
    }

    @Provides
    @Singleton
    List<ResolutionStep> steps(
            ConfigureSystemRepositoryStep configureSystemRepositoryStep,

            CollectPomFilesStep collectPomFilesStep,
            BuildPomIndexStep buildPomIndexStep,

            CollectDeclaredDependenciesStep collectDependenciesStep,
            CollectConfigurationMetadataStep collectConfigurationMetadataStep,
            ApplyBomsStep applyBomsStep,
            ResolveSystemArtifactsStep resolveSystemArtifactsStep,
            ResolveTransitivesAndScanMissingStep resolveTransitivesAndScanMissingStep,
            ConfigureArtifactsStep configureArtifactsStep,
            ApplySubstitutionStep applySubstitutionStep
    ) {
        return List.of(
                configureSystemRepositoryStep,
                collectPomFilesStep,
                buildPomIndexStep,
                collectDependenciesStep,
                collectConfigurationMetadataStep,
                applyBomsStep,
                resolveSystemArtifactsStep,
                resolveTransitivesAndScanMissingStep,
                configureArtifactsStep,
                applySubstitutionStep
        );
    }
}
