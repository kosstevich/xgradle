package org.altlinux.xgradle.impl.indexing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.indexing.PomIndex;
import org.altlinux.xgradle.api.indexing.PomIndexBuilder;

import java.nio.file.Path;
import java.util.List;

@Singleton
final class DefaultPomIndexBuilder implements PomIndexBuilder {

    private final PomIndex pomIndex;

    @Inject
    DefaultPomIndexBuilder(PomIndex pomIndex) {
        this.pomIndex = pomIndex;
    }

    @Override
    public PomIndex build(List<Path> pomFiles) {
        pomIndex.build(pomFiles);
        return pomIndex;
    }
}
