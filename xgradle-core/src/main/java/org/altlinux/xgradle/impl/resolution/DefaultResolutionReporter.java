package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.configurators.ArtifactConfigurator;
import org.altlinux.xgradle.impl.utils.logging.DependencyLogger;
import org.gradle.api.logging.Logger;

@Singleton
class DefaultResolutionReporter implements ResolutionReporter {

    private final ArtifactConfigurator artifactConfigurator;

    @Inject
    DefaultResolutionReporter(ArtifactConfigurator artifactConfigurator) {
        this.artifactConfigurator = artifactConfigurator;
    }

    @Override
    public void report(ResolutionContext resolutionContext) {
        Logger logger = resolutionContext.gradle.getRootProject().getLogger();
        DependencyLogger depLogger = new DependencyLogger();

        depLogger.logSection("\n===== APPLYING SYSTEM DEPENDENCY VERSIONS =====", logger);
        depLogger.logSection("Initial dependencies", logger);
        depLogger.logInitialDependencies(resolutionContext.projectDeps, logger);

        depLogger.logSection("Resolved system artifacts", logger);
        depLogger.logResolvedArtifacts(resolutionContext.systemArtifacts, logger);

        depLogger.logSection("Test resolutionContext dependencies", logger);
        depLogger.logTestContextDependencies(resolutionContext.testContextDependencies, logger);

        depLogger.logSection("===== DEPENDENCY RESOLUTION COMPLETED =====", logger);
        depLogger.logSection("Added artifacts to configurations", logger);
        depLogger.logConfigurationArtifacts(artifactConfigurator.getConfigurationArtifacts(), logger);

        if (!resolutionContext.notFound.isEmpty() || !resolutionContext.skipped.isEmpty()) {
            depLogger.logSection("Skipped dependencies", logger);
            depLogger.logSkippedDependencies(resolutionContext.notFound, resolutionContext.skipped, logger);
        }

        if (resolutionContext.substitutor != null) {
            resolutionContext.gradle.getTaskGraph().whenReady(taskGraph -> {
                depLogger.logSection("Dependency substitutions", logger);
                depLogger.logSubstitutions(
                        resolutionContext.substitutor.getOverrideLogs(),
                        resolutionContext.substitutor.getApplyLogs(),
                        logger
                );
            });
        }
    }
}
