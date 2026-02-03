package org.altlinux.xgradle.impl.controllers;

import com.beust.jcommander.JCommander;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.controllers.XmvnCompatController;
import org.altlinux.xgradle.api.registrars.Registrar;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Bom;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;

import org.slf4j.Logger;

import static org.altlinux.xgradle.impl.cli.CliPreconditions.require;

/**
 * Controller for managing XMvn compatibility functions for BOM artifacts.
 * Validates required CLI parameters and delegates registration to a BOM {@link Registrar}.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class DefaultBomXmvnCompatController implements XmvnCompatController {

    private final Registrar registrar;

    /**
     * Constructs a new BOM controller.
     *
     * @param registrar registrar for BOM artifacts
     */
    @Inject
    DefaultBomXmvnCompatController(@Bom Registrar registrar) {
        this.registrar = registrar;
    }

    /**
     * Configures and executes XMvn registration for BOM artifacts.
     * If BOM registration is requested but required parameters are missing, throws a CLI usage exception.
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
        if (!arguments.hasXmvnRegister() || !arguments.hasBomRegistration()) {
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
