package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.processors.BomProcessor;

import java.util.Map;

@Singleton
final class ApplyBomsStep implements ResolutionStep {

    private final BomProcessor bomProcessor;

    @Inject
    ApplyBomsStep(BomProcessor bomProcessor) {
        this.bomProcessor = bomProcessor;
    }

    @Override
    public String name() {
        return "apply-boms";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        BomProcessor.Context bomContext = new BomProcessor.Context(resolutionContext.gradle, resolutionContext.projectDeps);
        bomProcessor.process(bomContext);
        bomProcessor.removeBomsFromConfigurations(resolutionContext.gradle);

        resolutionContext.allDeps = bomContext.getProjectDependencies();

        for (Map.Entry<String, Boolean> e : resolutionContext.testDependencyFlags.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) {
                resolutionContext.testContextDependencies.add(e.getKey());
            }
        }

        bomProcessor.getBomManagedDeps().forEach((bomKey, deps) -> {
            String[] parts = bomKey.split(":");
            if (parts.length < 2) {
                return;
            }

            String bomId = parts[0] + ":" + parts[1];
            if (!resolutionContext.testDependencyFlags.getOrDefault(bomId, false)) {
                return;
            }

            for (String dep : deps) {
                String[] d = dep.split(":");
                if (d.length >= 2) {
                    resolutionContext.testContextDependencies.add(d[0] + ":" + d[1]);
                }
            }
        });
    }
}
