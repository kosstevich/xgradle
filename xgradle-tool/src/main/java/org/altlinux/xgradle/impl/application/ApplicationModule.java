package org.altlinux.xgradle.impl.application;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.application.Application;

public final class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Application.class).to(DefaultApplication.class);
    }
}
