package org.altlinux.xgradle.impl.controllers;

import com.beust.jcommander.JCommander;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.controllers.XmvnCompatController;
import org.altlinux.xgradle.api.registrars.Registrar;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Library;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;

import org.slf4j.Logger;

import static org.altlinux.xgradle.impl.cli.CliPreconditions.require;

/**
 * Controller for managing XMvn compatibility functions for library artifacts.
 * Validates required CLI parameters and delegates registration to a library {@link Registrar}.
 * BOM and Javadoc modes are ignored by this controller.
 *
 * @author Ivan Khanas
 */
@Singleton
class DefaultXmvnCompatController implements XmvnCompatController {

    private final Registrar registrar;

    /**
     * Constructs a new library controller.
     *
     * @param registrar registrar for library artifacts
     */
    @Inject
    DefaultXmvnCompatController(@Library Registrar registrar) {
        this.registrar = registrar;
    }

    /**
     * Configures and executes XMvn registration for library artifacts.
     * If library registration is requested but required parameters are missing, throws a CLI usage exception.
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
        if (!arguments.hasXmvnRegister()
                || arguments.hasBomRegistration()
                || arguments.hasJavadocRegistration()) {
            return;
        }

        require(arguments.hasSearchingDirectory(), "No searching directory specified");

        registrar.registerArtifacts(
                arguments.getSearchingDirectory(),
                arguments.getXmvnRegister(),
                arguments.getArtifactName()
        );
    }
}
