package org.altlinux.xgradle.impl.resolution;

import org.gradle.api.invocation.Gradle;

public interface SystemDependencyResolution {
    void run(Gradle gradle);
}
