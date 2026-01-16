package org.altlinux.xgradle.impl.resolution;

public interface ResolutionPipeline {
    ResolutionContext run(ResolutionContext ext);
}
