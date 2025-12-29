package org.altlinux.xgradle.impl.model;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.model.ArtifactFactory;

public final class ModelModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ArtifactFactory.class).to(DefaultArtifactFactory.class);
    }
}
