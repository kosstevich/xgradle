package org.altlinux.xgradle.api.managers;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.List;

public interface RepositoryManager {

    void setLogger(Logger logger);

    void configurePluginsRepository(Settings settings, File baseDir);

    void configureDependenciesRepository(RepositoryHandler repositories);

    List<File> scanDirectories(File baseDir);
}
