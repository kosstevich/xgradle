package org.altlinux.xgradle.impl;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.altlinux.xgradle.api.application.Application;
import org.altlinux.xgradle.impl.di.XGradleToolModule;

/**
 * Main entry point for the XGradle tool application.
 * Handles dependency injection setup and delegates execution to {@link Application}.
 *
 * @author Ivan Khanas
 */
public class Main {

    /**
     * Main method that serves as the application entry point.
     * Exits the process with the exit code returned by the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new XGradleToolModule());
        int code = injector.getInstance(Application.class).run(args).getExitCode();
        System.exit(code);
    }
}
