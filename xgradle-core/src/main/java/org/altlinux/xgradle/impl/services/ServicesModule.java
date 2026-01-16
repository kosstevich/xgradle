package org.altlinux.xgradle.impl.services;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.services.ArtifactVerifier;
import org.altlinux.xgradle.api.services.VersionScanner;

public final class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(VersionScanner.class).to(DependencyVersionScanner.class);
        bind(ArtifactVerifier.class).to(FileSystemArtifactVerifier.class);
    }
}
