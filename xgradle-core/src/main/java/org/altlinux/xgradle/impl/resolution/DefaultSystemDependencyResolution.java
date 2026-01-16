package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.gradle.api.invocation.Gradle;

@Singleton
class DefaultSystemDependencyResolution implements SystemDependencyResolution {

    private final DefaultResolutionPipeline pipeline;
    private final ResolutionReporter reporter;

    @Inject
    DefaultSystemDependencyResolution(DefaultResolutionPipeline pipeline, ResolutionReporter reporter) {
        this.pipeline = pipeline;
        this.reporter = reporter;
    }

    @Override
    public void run(Gradle gradle) {
        ResolutionContext ctx = new ResolutionContext(gradle);
        pipeline.run(ctx);
        reporter.report(ctx);
    }
}
