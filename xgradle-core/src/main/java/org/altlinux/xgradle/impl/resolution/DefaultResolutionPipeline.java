package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

@Singleton
class DefaultResolutionPipeline implements ResolutionPipeline {

    private final List<ResolutionStep> resolutionSteps;

    @Inject
    DefaultResolutionPipeline (List<ResolutionStep> resolutionSteps) {
        this.resolutionSteps = resolutionSteps;
    }

    @Override
    public ResolutionContext run(ResolutionContext ext) {
        for (ResolutionStep step : resolutionSteps) {
            step.execute(ext);
        }
        return ext;
    }

}
