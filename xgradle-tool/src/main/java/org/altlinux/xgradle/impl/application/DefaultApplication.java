package org.altlinux.xgradle.impl.application;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.application.Application;
import org.altlinux.xgradle.api.controllers.ArtifactsInstallationController;
import org.altlinux.xgradle.api.controllers.PomRedactionController;
import org.altlinux.xgradle.api.controllers.XmvnCompatController;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Bom;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Javadoc;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Library;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;
import org.altlinux.xgradle.impl.cli.commands.CliVersion;
import org.altlinux.xgradle.impl.enums.ExitCode;
import org.altlinux.xgradle.impl.exceptions.CliUsageException;

import org.slf4j.Logger;

import javax.inject.Provider;
import java.io.FileNotFoundException;

/**
 * Default implementation of {@link Application}.
 * Parses CLI arguments and coordinates controllers execution.
 *
 * @author Ivan Khanas
 */
@Singleton
final class DefaultApplication implements Application {

    private final JCommander jCommander;
    private final CliArgumentsContainer cliArgs;
    private final Logger logger;

    private final Provider<XmvnCompatController> libraryXmvnController;
    private final Provider<XmvnCompatController> bomXmvnController;
    private final Provider<XmvnCompatController> javadocXmvnController;
    private final Provider<ArtifactsInstallationController> pluginsController;
    private final Provider<PomRedactionController> pomRedactionController;

    /**
     * Constructs the application with required dependencies.
     *
     * @param jCommander JCommander instance used to parse and print usage
     * @param cliArgs parsed arguments container
     * @param logger application logger
     * @param libraryXmvnController controller for library registration
     * @param bomXmvnController controller for BOM registration
     * @param javadocXmvnController controller for Javadoc installation
     * @param pluginsController controller for plugin artifacts installation
     * @param pomRedactionController controller for POM redaction operations
     */
    @Inject
    DefaultApplication(
            JCommander jCommander,
            CliArgumentsContainer cliArgs,
            Logger logger,
            @Library Provider<XmvnCompatController> libraryXmvnController,
            @Bom Provider<XmvnCompatController> bomXmvnController,
            @Javadoc Provider<XmvnCompatController> javadocXmvnController,
            Provider<ArtifactsInstallationController> pluginsController,
            Provider<PomRedactionController> pomRedactionController
    ) {
        this.jCommander = jCommander;
        this.cliArgs = cliArgs;
        this.logger = logger;
        this.libraryXmvnController = libraryXmvnController;
        this.bomXmvnController = bomXmvnController;
        this.javadocXmvnController = javadocXmvnController;
        this.pluginsController = pluginsController;
        this.pomRedactionController = pomRedactionController;
    }

    /**
     * Runs the application.
     * Returns an {@link ExitCode} describing the overall result.
     *
     * @param args raw command-line arguments
     * @return resulting exit code
     */
    @Override
    public ExitCode run(String[] args) {
        try {
            jCommander.parse(args);
            cliArgs.validateMutuallyExclusive();
        } catch (ParameterException e) {
            logger.error(e.getMessage());
            jCommander.usage();
            return ExitCode.ERROR;
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return ExitCode.ERROR;
        }

        if (args.length == 0 || cliArgs.hasHelp()) {
            jCommander.usage();
            return ExitCode.SUCCESS;
        }

        if (cliArgs.hasPomRedaction()) {
            try {
                pomRedactionController.get().configure();
                return ExitCode.SUCCESS;
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
                return ExitCode.ERROR;
            }
        }

        if (cliArgs.hasVersion()) {
            try {
                new CliVersion().printVersion();
                return ExitCode.SUCCESS;
            } catch (FileNotFoundException e) {
                logger.error("Could not find application.properties", e);
                return ExitCode.ERROR;
            }
        }

        try {
            libraryXmvnController.get().configureXmvnCompatFunctions(jCommander, args, cliArgs, logger);
            pluginsController.get().configurePluginArtifactsInstallation(jCommander, args, cliArgs, logger);
            bomXmvnController.get().configureXmvnCompatFunctions(jCommander, args, cliArgs, logger);
            javadocXmvnController.get().configureXmvnCompatFunctions(jCommander, args, cliArgs, logger);
            return ExitCode.SUCCESS;
        } catch (CliUsageException e) {
            logger.error(e.getMessage());
            jCommander.usage();
            return ExitCode.ERROR;
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return ExitCode.ERROR;
        }
    }
}
