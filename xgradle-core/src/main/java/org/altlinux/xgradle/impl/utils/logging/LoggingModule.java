package org.altlinux.xgradle.impl.utils.logging;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public final class LoggingModule extends AbstractModule {

    @Provides
    @Singleton
    Logger provideLogger() {
        return Logging.getLogger("XGradleLogger");
    }
}
