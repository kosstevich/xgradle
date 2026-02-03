package org.altlinux.xgradle.api.collectors;

import org.altlinux.xgradle.impl.model.ConfigurationInfoSnapshot;
import org.gradle.api.invocation.Gradle;

/**
 * Collects configuration-level dependency information for the current build.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
public interface ConfigurationInfoCollector {

    ConfigurationInfoSnapshot collect(Gradle gradle);
}
