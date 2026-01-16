package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.collectors.ConfigurationInfoCollector;

@Singleton
final class CollectConfigurationMetadataStep implements ResolutionStep {

    private final ConfigurationInfoCollector configurationInfoCollector;

    @Inject
    CollectConfigurationMetadataStep(ConfigurationInfoCollector configurationInfoCollector) {
        this.configurationInfoCollector = configurationInfoCollector;

    }

    @Override
    public String name() {
        return "collect-configuration-metadata";
    }

    @Override
    public void execute(ResolutionContext resolutionContext) {
        configurationInfoCollector.collect(resolutionContext.gradle);
        resolutionContext.testDependencyFlags = configurationInfoCollector.getTestDependencyFlags();
        resolutionContext.dependencyConfigurations = configurationInfoCollector.getDependencyConfigurations();
        resolutionContext.dependencyConfigNames = configurationInfoCollector.getDependencyConfigNames();
    }
}
