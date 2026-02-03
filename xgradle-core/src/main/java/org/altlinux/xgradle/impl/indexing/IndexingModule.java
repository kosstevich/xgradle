package org.altlinux.xgradle.impl.indexing;

import com.google.inject.AbstractModule;
import org.altlinux.xgradle.api.indexing.PomIndex;
import org.altlinux.xgradle.api.indexing.PomIndexBuilder;

public final class IndexingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PomIndex.class).to(DefaultPomIndex.class);
        bind(PomIndexBuilder.class).to(DefaultPomIndexBuilder.class);
    }
}
