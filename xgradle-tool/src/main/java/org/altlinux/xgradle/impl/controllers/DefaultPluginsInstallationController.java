package org.altlinux.xgradle.impl.controllers;

import com.beust.jcommander.JCommander;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.controllers.ArtifactsInstallationController;
import org.altlinux.xgradle.api.installers.ArtifactsInstaller;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;
import org.altlinux.xgradle.impl.enums.ProcessingType;

import org.slf4j.Logger;

import static org.altlinux.xgradle.impl.cli.CliPreconditions.require;

/**
 * Controller for managing Gradle plugin installation process.
 * Validates required CLI parameters and delegates installation to {@link ArtifactsInstaller}.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class DefaultPluginsInstallationController implements ArtifactsInstallationController {

    private final ArtifactsInstaller pluginArtifactsInstaller;

    @Inject
    DefaultPluginsInstallationController(ArtifactsInstaller pluginArtifactsInstaller) {
        this.pluginArtifactsInstaller = pluginArtifactsInstaller;
    }

    @Override
    public void configurePluginArtifactsInstallation(
            JCommander jCommander,
            String[] args,
            CliArgumentsContainer arguments,
            Logger logger
    ) {
        if (!arguments.hasInstallPluginParameter()) {
            return;
        }

        require(arguments.hasSearchingDirectory(), "No searching directory specified");
        require(arguments.hasArtifactName(), "Please specify an artifact name. (--artifact=<artifactName>)");
        require(arguments.hasPomInstallationDirectory(), "No POM installation directory specified");
        require(arguments.hasJarInstallationDirectory(), "No JAR installation directory specified");

        pluginArtifactsInstaller.install(
                arguments.getSearchingDirectory(),
                arguments.getArtifactName(),
                arguments.getPomInstallationDirectory(),
                arguments.getJarInstallationDirectory(),
                ProcessingType.PLUGINS
        );
    }
}
