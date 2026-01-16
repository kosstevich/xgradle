package org.altlinux.xgradle.impl.collectors;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.collectors.ConfigurationInfoCollector;
import org.altlinux.xgradle.api.collectors.DependencyCollector;
import org.altlinux.xgradle.api.collectors.PomFilesCollector;

public final class CollectorsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConfigurationInfoCollector.class).to(DefaultConfigurationInfoCollector.class);
        bind(DependencyCollector.class).to(DefaultDependencyCollector.class);
        bind(PomFilesCollector.class).to(DefaultPomFilesCollector.class);
    }
}
