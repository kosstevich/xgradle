package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.processors.TransitiveProcessor;
import org.altlinux.xgradle.api.services.VersionScanner;
import org.altlinux.xgradle.impl.model.MavenCoordinate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
final class ResolveTransitivesAndScanMissingStep implements ResolutionStep {

    private final TransitiveProcessor transitiveProcessor;
    private final VersionScanner versionScanner;

    @Inject
    ResolveTransitivesAndScanMissingStep(TransitiveProcessor transitiveProcessor, VersionScanner versionScanner) {
        this.transitiveProcessor = transitiveProcessor;
        this.versionScanner = versionScanner;
    }

    @Override
    public String name() {
        return "resolve-transitives-and-scan-missing";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        transitiveProcessor.setTestContextDependencies(resolutionContext.testContextDependencies);
        transitiveProcessor.process(resolutionContext.systemArtifacts);

        resolutionContext.testContextDependencies.addAll(transitiveProcessor.getTestDependencies());
        resolutionContext.skipped = transitiveProcessor.getSkippedDependencies();

        Set<String> mainDeps = transitiveProcessor.getMainDependencies();
        Set<String> testDeps = transitiveProcessor.getTestDependencies();

        Set<String> newMain = new HashSet<>(mainDeps);
        newMain.removeAll(resolutionContext.systemArtifacts.keySet());

        if (!newMain.isEmpty()) {
            Map<String, MavenCoordinate> mainArtifacts =
                    versionScanner.scanSystemArtifacts(newMain);
            resolutionContext.systemArtifacts.putAll(mainArtifacts);
        }

        Set<String> newTest = new HashSet<>(testDeps);
        newTest.removeAll(resolutionContext.systemArtifacts.keySet());
        if (!newTest.isEmpty()) {
            Map<String, MavenCoordinate> testArtifacts =
                    versionScanner.scanSystemArtifacts(newTest);
            resolutionContext.systemArtifacts.putAll(testArtifacts);
        }
    }
}
