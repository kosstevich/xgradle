package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.collectors.PomFilesCollector;

import java.nio.file.Path;

@Singleton
final class CollectPomFilesStep implements ResolutionStep {

    private final Provider<PomFilesCollector> collectorProvider;

    @Inject
    CollectPomFilesStep(Provider<PomFilesCollector> collectorProvider) {
        this.collectorProvider = collectorProvider;
    }

    @Override
    public String name() {
        return "collect-pom-files";
    }

    @Override
    public void execute(ResolutionContext context) {
        PomFilesCollector collector = collectorProvider.get();

        Path rootDir = Path.of(System.getProperty("maven.poms.dir", ""));
        context.pomFiles = collector.collect(rootDir);
    }
}
