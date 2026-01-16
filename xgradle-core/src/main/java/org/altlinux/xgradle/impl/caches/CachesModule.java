package org.altlinux.xgradle.impl.caches;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.caches.PomDataCache;

public class CachesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PomDataCache.class).to(DefaultPomDataCache.class);
    }
}
