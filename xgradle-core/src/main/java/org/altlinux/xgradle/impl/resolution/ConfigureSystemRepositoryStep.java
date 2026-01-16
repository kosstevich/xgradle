package org.altlinux.xgradle.impl.resolution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.managers.RepositoryManager;
import org.altlinux.xgradle.impl.extensions.SystemDepsExtension;

import java.io.File;

@Singleton
final class ConfigureSystemRepositoryStep implements ResolutionStep {

    private final RepositoryManager repositoryManager;

    @Inject
    ConfigureSystemRepositoryStep(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Override
    public String name() {
        return "configure-system-repository";
    }

    @Override
    public void execute(ResolutionContext ctx) {
        ctx.gradle.allprojects(project ->
                repositoryManager.configureDependenciesRepository(
                        project.getRepositories(),
                        new File(SystemDepsExtension.getJarsPath())
                )
        );
    }
}
