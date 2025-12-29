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
package org.altlinux.xgradle.impl;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.altlinux.xgradle.api.application.Application;

import org.altlinux.xgradle.impl.controllers.*;
import org.altlinux.xgradle.impl.di.XGradleToolModule;


/**
 * Main entry point for the XGradle tool application.
 * Handles command-line argument parsing, dependency injection setup, and controller coordination.
 *
 * @author Ivan Khanas
 */
public class Main {

    /**
     * Main method that serves as the application entry point.
     * Parses command-line arguments, sets up dependency injection, and coordinates controllers.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new XGradleToolModule());
        injector.getInstance(Application.class).run(args).exit();
    }
}