package org.altlinux.xgradle.impl.model;

import com.google.inject.Inject;

import com.google.inject.Singleton;
import org.altlinux.xgradle.api.model.ArtifactCoordinates;
import org.altlinux.xgradle.api.model.ArtifactData;
import org.altlinux.xgradle.api.model.ArtifactFactory;
import org.apache.maven.model.Model;

import java.nio.file.Path;

@Singleton
public final class DefaultArtifactFactory implements ArtifactFactory {

    @Inject
    DefaultArtifactFactory() {}

    @Override
    public ArtifactCoordinates coordinates(String groupId, String artifactId, String version) {
        return new DefaultArtifactCoordinates(groupId, artifactId, version);
    }

    @Override
    public ArtifactData data(ArtifactCoordinates coordinates, Model model, Path pomPath, Path jarPath) {
        return new DefaultArtifactData(coordinates, model, pomPath, jarPath);
    }
}
