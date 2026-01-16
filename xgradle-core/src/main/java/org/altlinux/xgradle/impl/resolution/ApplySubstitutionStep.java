package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.processors.BomProcessor;
import org.altlinux.xgradle.impl.resolvers.DependencySubstitutor;

@Singleton
final class ApplySubstitutionStep implements ResolutionStep {

    private final BomProcessor bomProcessor;

    @Inject
    ApplySubstitutionStep(BomProcessor bomProcessor) {
        this.bomProcessor = bomProcessor;
    }

    @Override
    public String name() {
        return "apply-substitution";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        DependencySubstitutor substitutor = new DependencySubstitutor(
                resolutionContext.requestedVersions,
                resolutionContext.systemArtifacts,
                bomProcessor.getManagedVersions()
        );
        substitutor.configure(resolutionContext.gradle);
        resolutionContext.substitutor = substitutor;
    }
}
