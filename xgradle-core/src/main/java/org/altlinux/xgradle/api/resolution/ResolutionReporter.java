package org.altlinux.xgradle.api.resolution;

import org.altlinux.xgradle.impl.resolution.ResolutionContext;

public interface ResolutionReporter {
    void report(ResolutionContext ctx);
}
