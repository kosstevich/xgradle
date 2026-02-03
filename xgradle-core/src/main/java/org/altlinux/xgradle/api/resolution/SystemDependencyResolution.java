package org.altlinux.xgradle.api.resolution;

import org.gradle.api.invocation.Gradle;

public interface SystemDependencyResolution {
    void run(Gradle gradle);
}
