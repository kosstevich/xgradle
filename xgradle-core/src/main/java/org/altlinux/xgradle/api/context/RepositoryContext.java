package org.altlinux.xgradle.api.context;

import org.gradle.api.initialization.Settings;

import java.io.File;
import java.util.Objects;


public final class RepositoryContext {

    private final Settings settings;
    private final File baseDir;

    public RepositoryContext(Settings settings, File baseDir) {
        this.settings = Objects.requireNonNull(settings);
        this.baseDir = Objects.requireNonNull(baseDir);
    }

    public Settings getSettings() {
        return settings;
    }

    public File getBaseDir() {
        return baseDir;
    }
}
