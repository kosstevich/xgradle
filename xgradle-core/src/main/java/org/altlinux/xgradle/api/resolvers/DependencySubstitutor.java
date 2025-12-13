package org.altlinux.xgradle.api.resolvers;

import org.gradle.api.invocation.Gradle;

import java.util.Map;

public interface DependencySubstitutor {

    void configure(Gradle gradle);

    Map<String, String> getOverrideLogs();

    Map<String, String> getApplyLogs();
}
