package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.indexing.PomIndexBuilder;

@Singleton
final class BuildPomIndexStep implements ResolutionStep {

    private final PomIndexBuilder builder;

    @Inject
    BuildPomIndexStep(PomIndexBuilder builder) {
        this.builder = builder;
    }

    @Override
    public String name() {
        return "build-pom-index";
    }

    @Override
    public void execute(ResolutionContext context) {
        context.pomIndex = builder.build(context.pomFiles);
    }
}
