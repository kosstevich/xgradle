package org.altlinux.xgradle.impl.application;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;

import com.google.inject.Singleton;
import org.altlinux.xgradle.api.application.Application;
import org.altlinux.xgradle.api.controllers.ArtifactsInstallationController;
import org.altlinux.xgradle.api.controllers.XmvnCompatController;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Bom;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Javadoc;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Library;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;
import org.altlinux.xgradle.impl.cli.commands.CliVersion;
import org.altlinux.xgradle.impl.enums.ExitCode;
import org.slf4j.Logger;

import javax.inject.Provider;
import java.io.FileNotFoundException;

@Singleton
final class DefaultApplication implements Application {

    private final JCommander jCommander;
    private final CliArgumentsContainer cliArgs;
    private final Logger logger;

    private final Provider<XmvnCompatController> libraryXmvnController;
    private final Provider<XmvnCompatController> bomXmvnController;
    private final Provider<XmvnCompatController> javadocXmvnController;
    private final Provider<ArtifactsInstallationController> pluginsController;

    @Inject
    DefaultApplication(
            JCommander jCommander,
            CliArgumentsContainer cliArgs,
            Logger logger,
            @Library Provider<XmvnCompatController> libraryXmvnController,
            @Bom Provider<XmvnCompatController> bomXmvnController,
            @Javadoc Provider<XmvnCompatController> javadocXmvnController,
            Provider<ArtifactsInstallationController> pluginsController
    ) {
        this.jCommander = jCommander;
        this.cliArgs = cliArgs;
        this.logger = logger;
        this.libraryXmvnController = libraryXmvnController;
        this.bomXmvnController = bomXmvnController;
        this.javadocXmvnController = javadocXmvnController;
        this.pluginsController = pluginsController;
    }

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
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return ExitCode.ERROR;
        }
    }
}
