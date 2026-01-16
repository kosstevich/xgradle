package org.altlinux.xgradle.impl.handlers;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.handlers.PluginsDependenciesHandler;
import org.altlinux.xgradle.api.handlers.ProjectDependenciesHandler;

public final class HandlersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PluginsDependenciesHandler.class).to(DefaultPluginsDependenciesHandler.class);
        bind(ProjectDependenciesHandler.class).to(DefaultProjectDependenciesHandler.class);
    }
}
