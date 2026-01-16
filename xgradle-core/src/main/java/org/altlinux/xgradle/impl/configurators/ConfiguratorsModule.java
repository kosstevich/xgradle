package org.altlinux.xgradle.impl.configurators;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.configurators.ArtifactConfigurator;

public final class ConfiguratorsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ArtifactConfigurator.class).to(DefaultArtifactConfigurator.class);
    }
}
