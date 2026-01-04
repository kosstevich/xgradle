/*
 * Copyright 2025 BaseALT Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.altlinux.xgradle.impl.registrars;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Bom;
import org.altlinux.xgradle.impl.enums.ExitCode;
import org.altlinux.xgradle.api.cli.CommandExecutor;
import org.altlinux.xgradle.api.cli.CommandLineParser;
import org.altlinux.xgradle.api.processors.PomProcessor;
import org.altlinux.xgradle.api.registrars.Registrar;

import org.altlinux.xgradle.impl.exceptions.CommandExecutionException;
import org.altlinux.xgradle.impl.exceptions.EmptyRegisterCommandException;
import org.altlinux.xgradle.impl.exceptions.RegistrationFailedException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.Set;

/**
 * Registrar implementation for XMvn compatibility with BOM artifacts.
 * Handles registration of BOM files using XMvn commands.
 *
 * @author Ivan Khanas
 */
@Singleton
class XmvnBomCompatRegistrar implements Registrar {

    private final PomProcessor<Set<Path>> pomProcessor;
    private final CommandExecutor commandExecutor;
    private final CommandLineParser commandLineParser;
    private final Logger logger;

    /**
     * Constructs a new DefaultXmvnBomCompatRegistrar with required dependencies.
     *
     * @param pomProcessor processor for BOM POM files
     * @param commandExecutor executor for command execution
     * @param commandLineParser parser for command-line parsing
     * @param logger logger instance
     */
    @Inject
    XmvnBomCompatRegistrar(
            @Bom PomProcessor<Set<Path>> pomProcessor,
            CommandExecutor commandExecutor,
            CommandLineParser commandLineParser,
            Logger logger
    ) {
        this.pomProcessor = pomProcessor;
        this.commandExecutor = commandExecutor;
        this.commandLineParser = commandLineParser;
        this.logger = logger;
    }

    /**
     * Registers BOM artifacts from the specified directory using XMvn commands.
     *
     * @param searchingDir the directory to search for BOM files
     * @param command the XMvn registration command to use
     * @param artifactName optional list of artifact names to filter by
     * @throws RuntimeException if command execution fails
     */
    @Override
    public void registerArtifacts(String searchingDir, String command, Optional<List<String>> artifactName) {
        Set<Path> artifacts;

        if (artifactName.isPresent()) {
            artifacts = pomProcessor.pomsFromDirectory(searchingDir, artifactName);
        }else {
            artifacts = pomProcessor.pomsFromDirectory(searchingDir, Optional.empty());
        }

        List<String> commandParts = commandLineParser.parseCommandLine(command);

        if (commandParts == null || commandParts.isEmpty()) {
            throw new EmptyRegisterCommandException(command);
        }

        for (Path part : artifacts) {
            List<String> currentCommand = new ArrayList<>(commandParts);
            currentCommand.add(part.toString());
            logger.info("Registering BOM: " + String.join(" ", currentCommand));

            ProcessBuilder processBuilder = new ProcessBuilder(currentCommand);

            try {
                int exitCode = commandExecutor.execute(processBuilder);

                if (exitCode != ExitCode.SUCCESS.getExitCode()) {
                    throw new RegistrationFailedException(currentCommand, exitCode);
                }
            } catch (IOException | InterruptedException e) {
                throw new CommandExecutionException(currentCommand, e);
            }
        }

        if (artifacts.isEmpty()) {
            logger.info("No BOM registered");
        }else {
            logger.info("BOM`s registered successfully");
        }
    }
}
