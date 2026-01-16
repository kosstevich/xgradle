package org.altlinux.xgradle.api.managers;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.initialization.Settings;

import java.io.File;

public interface RepositoryManager {

    void configurePluginsRepository(Settings settings, File baseDir);

    void configureDependenciesRepository(RepositoryHandler repositories, File baseDir);
}
