package org.altlinux.xgradle.impl.controllers;

import com.beust.jcommander.JCommander;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.controllers.XmvnCompatController;
import org.altlinux.xgradle.api.installers.JavadocInstaller;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;

import org.slf4j.Logger;

import static org.altlinux.xgradle.impl.cli.CliPreconditions.require;

/**
 * Controller for managing Javadoc installation.
 * Validates required CLI parameters and delegates work to {@link JavadocInstaller}.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class DefaultJavadocXmvnCompatController implements XmvnCompatController {

    private final JavadocInstaller javadocInstaller;

    @Inject
    DefaultJavadocXmvnCompatController(JavadocInstaller javadocInstaller) {
        this.javadocInstaller = javadocInstaller;
    }

    @Override
    public void configureXmvnCompatFunctions(
            JCommander jCommander,
            String[] args,
            CliArgumentsContainer arguments,
            Logger logger
    ) {
        if (!arguments.hasJavadocRegistration()) {
            return;
        }

        require(arguments.hasSearchingDirectory(), "No searching directory specified for Javadoc installation");
        require(arguments.hasJarInstallationDirectory(), "No JAR installation directory specified for Javadoc installation");

        javadocInstaller.installJavadoc(
                arguments.getSearchingDirectory(),
                arguments.getArtifactName(),
                arguments.getJarInstallationDirectory()
        );
    }
}
