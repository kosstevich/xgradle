package org.altlinux.xgradle.impl.indexing;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.indexing.PomIndex;

public final class IndexingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PomIndex.class).to(DefaultPomIndex.class);
    }
}
