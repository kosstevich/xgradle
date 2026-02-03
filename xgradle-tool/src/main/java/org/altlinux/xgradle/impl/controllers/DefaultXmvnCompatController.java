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
 * @author Ivan Khanas <xeno@altlinux.org>
 */
@Singleton
final class DefaultXmvnCompatController implements XmvnCompatController {

    private final Registrar registrar;

    @Inject
    DefaultXmvnCompatController(@Library Registrar registrar) {
        this.registrar = registrar;
    }

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
