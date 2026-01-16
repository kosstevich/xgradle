package org.altlinux.xgradle.impl.parsers;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.parsers.PomParser;

public final class ParsersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PomParser.class).to(DefaultPomParser.class);
    }
}
