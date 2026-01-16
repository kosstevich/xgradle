package org.altlinux.xgradle.impl.resolution;

public interface ResolutionStep {
    String name();

    void execute(ResolutionContext ctx);
}
