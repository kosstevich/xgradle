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
 * @author Ivan Khanas
 */
@Singleton
class DefaultJavadocXmvnCompatController implements XmvnCompatController {

    private final JavadocInstaller javadocInstaller;

    /**
     * Constructs a new controller.
     *
     * @param javadocInstaller installer for Javadoc artifacts
     */
    @Inject
    DefaultJavadocXmvnCompatController(JavadocInstaller javadocInstaller) {
        this.javadocInstaller = javadocInstaller;
    }

    /**
     * Configures Javadoc installation based on command-line arguments.
     * If Javadoc mode is enabled but required parameters are missing, throws a CLI usage exception.
     *
     * @param jCommander command-line parser (not used on success path)
     * @param args raw command-line args (not used)
     * @param arguments parsed command-line arguments container
     * @param logger logger instance (not used on success path)
     */
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
