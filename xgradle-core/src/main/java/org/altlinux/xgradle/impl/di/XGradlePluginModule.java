package org.altlinux.xgradle.impl.di;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.impl.caches.CachesModule;
import org.altlinux.xgradle.impl.collectors.CollectorsModule;
import org.altlinux.xgradle.impl.configurators.ConfiguratorsModule;
import org.altlinux.xgradle.impl.handlers.HandlersModule;
import org.altlinux.xgradle.impl.managers.ManagersModule;
import org.altlinux.xgradle.impl.maven.MavenModule;
import org.altlinux.xgradle.impl.processors.ProcessorsModule;
import org.altlinux.xgradle.impl.resolution.ResolutionModule;
import org.altlinux.xgradle.impl.services.ServicesModule;
import org.altlinux.xgradle.impl.utils.logging.LoggingModule;

public class XGradlePluginModule extends AbstractModule {

    @Override
    protected void configure() {

        install(new LoggingModule());

        install(new CachesModule());

        install(new CollectorsModule());

        install(new ProcessorsModule());

        install(new ManagersModule());

        install(new ServicesModule());

        install(new MavenModule());

        install(new HandlersModule());

        install(new ConfiguratorsModule());

        install(new ResolutionModule());
    }
}
