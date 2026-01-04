package unittests.controllers;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.altlinux.xgradle.api.controllers.PomRedactionController;
import org.altlinux.xgradle.api.installers.ArtifactsInstaller;
import org.altlinux.xgradle.api.installers.JavadocInstaller;
import org.altlinux.xgradle.api.registrars.Registrar;
import org.altlinux.xgradle.api.services.PomService;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Bom;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Library;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;
import org.altlinux.xgradle.impl.config.ToolConfig;
import org.altlinux.xgradle.impl.controllers.ControllersModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PomRedactionController contract")
class PomRedactionControllerTests {

    private PomRedactionController controller;

    @Mock
    private PomService pomService;

    @Mock
    private CliArgumentsContainer cliArgs;

    @Mock
    private ArtifactsInstaller artifactsInstaller;

    @Mock
    private JavadocInstaller javadocInstaller;

    @Mock
    private Registrar libraryRegistrar;

    @Mock
    private Registrar bomRegistrar;

    @BeforeEach
    void setUp() {
        ToolConfig toolConfig = new ToolConfig(cliArgs);

        Injector injector = Guice.createInjector(
                Modules.override(new ControllersModule())
                        .with(new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(PomService.class).toInstance(pomService);
                                bind(CliArgumentsContainer.class).toInstance(cliArgs);
                                bind(ToolConfig.class).toInstance(toolConfig);

                                bind(ArtifactsInstaller.class).toInstance(artifactsInstaller);
                                bind(JavadocInstaller.class).toInstance(javadocInstaller);

                                bind(Registrar.class).annotatedWith(Library.class).toInstance(libraryRegistrar);
                                bind(Registrar.class).annotatedWith(Bom.class).toInstance(bomRegistrar);
                            }
                        })
        );

        controller = injector.getInstance(PomRedactionController.class);
    }

    @Test
    @DisplayName("Is created by Guice")
    void createdByGuice() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("configure: removeDependencies delegates to PomService.removeDependency for each coord")
    void removeDependenciesDelegatesForEach() {
        when(cliArgs.getSearchingDirectory()).thenReturn("/repo");
        when(cliArgs.hasRemoveDependencies()).thenReturn(true);
        when(cliArgs.getRemoveDependencies()).thenReturn(List.of("g:a", "g:b"));
        when(cliArgs.isRecursive()).thenReturn(true);

        controller.configure();

        Path repo = Path.of("/repo");
        verify(pomService).removeDependency(repo, "g:a", true);
        verify(pomService).removeDependency(repo, "g:b", true);
        verifyNoMoreInteractions(pomService);
    }

    @Test
    @DisplayName("configure: addDependencies delegates to PomService.addDependency for each coord")
    void addDependenciesDelegatesForEach() {
        when(cliArgs.getSearchingDirectory()).thenReturn("/repo");
        when(cliArgs.hasAddDependencies()).thenReturn(true);
        when(cliArgs.getAddDependencies()).thenReturn(List.of("g:a:1", "g:b:2"));

        when(cliArgs.isRecursive()).thenReturn(false);

        controller.configure();

        Path repo = Path.of("/repo");
        verify(pomService).addDependency(repo, "g:a:1", false);
        verify(pomService).addDependency(repo, "g:b:2", false);
        verifyNoMoreInteractions(pomService);
    }

    @Test
    @DisplayName("configure: changeDependencies delegates once with pair[0], pair[1]")
    void changeDependenciesDelegatesOnce() {
        when(cliArgs.getSearchingDirectory()).thenReturn("/repo");
        when(cliArgs.hasChangeDependencies()).thenReturn(true);
        when(cliArgs.getChangeDependencies()).thenReturn(List.of("g:a:1", "g:b:2"));

        when(cliArgs.isRecursive()).thenReturn(true);

        controller.configure();

        verify(pomService).changeDependency(Path.of("/repo"), "g:a:1", "g:b:2", true);
        verifyNoMoreInteractions(pomService);
    }
}
