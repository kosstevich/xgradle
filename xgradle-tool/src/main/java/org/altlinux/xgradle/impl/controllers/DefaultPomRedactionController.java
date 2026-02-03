package org.altlinux.xgradle.impl.controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.altlinux.xgradle.api.controllers.PomRedactionController;
import org.altlinux.xgradle.impl.config.ToolConfig;
import org.altlinux.xgradle.api.services.PomService;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;

import java.nio.file.Path;
import java.util.List;

@Singleton
final class DefaultPomRedactionController implements PomRedactionController {

    private final ToolConfig toolConfig;
    private final PomService pomService;
    private final CliArgumentsContainer arguments;

    @Inject
    DefaultPomRedactionController(ToolConfig toolConfig, PomService pomService, CliArgumentsContainer arguments) {
        this.toolConfig = toolConfig;
        this.pomService = pomService;
        this.arguments = arguments;
    }

    @Override
    public void configure() {
        Path path = Path.of(arguments.getSearchingDirectory());
        boolean recursive = toolConfig.isRecursive();

        if (arguments.hasRemoveDependencies()) {
            arguments.getRemoveDependencies().forEach(c -> pomService.removeDependency(path, c, recursive));
            return;
        }

        if (arguments.hasAddDependencies()) {
            arguments.getAddDependencies().forEach(c -> pomService.addDependency(path, c, recursive));
            return;
        }

        if (arguments.hasChangeDependencies()) {
            List<String> pair = arguments.getChangeDependencies();
            pomService.changeDependency(path, pair.get(0), pair.get(1), recursive);
        }
    }
}
