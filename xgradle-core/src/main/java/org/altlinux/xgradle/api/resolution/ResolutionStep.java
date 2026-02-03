package org.altlinux.xgradle.api.resolution;

import org.altlinux.xgradle.impl.resolution.ResolutionContext;

public interface ResolutionStep {
    String name();

    void execute(ResolutionContext ctx);
}
