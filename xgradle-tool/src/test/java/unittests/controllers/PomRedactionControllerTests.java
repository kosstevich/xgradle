package unittests.controllers;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PomRedactionController contract")
class PomRedactionControllerTests {

    @Mock
    ToolConfig toolConfig;

    @Mock
    PomService pomService;

    @Mock
    CliArgumentsContainer arguments;

    @Mock
    ArtifactsInstaller artifactsInstaller;

    @Mock
    JavadocInstaller javadocInstaller;

    @Mock
    @SuppressWarnings("rawtypes")
    Registrar libraryRegistrar;

    @Mock
    @SuppressWarnings("rawtypes")
    Registrar bomRegistrar;

    private PomRedactionController controller;

    @BeforeEach
    void setUp() {
        Injector injector = Guice.createInjector(
                Modules.override(new ControllersModule())
                        .with(new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(ToolConfig.class).toInstance(toolConfig);
                                bind(PomService.class).toInstance(pomService);
                                bind(CliArgumentsContainer.class).toInstance(arguments);

                                bind(ArtifactsInstaller.class).toInstance(artifactsInstaller);
                                bind(JavadocInstaller.class).toInstance(javadocInstaller);

                                bind(Key.get(Registrar.class, Library.class)).toInstance(libraryRegistrar);
                                bind(Key.get(Registrar.class, Bom.class)).toInstance(bomRegistrar);
                            }
                        })
        );

        controller = injector.getInstance(PomRedactionController.class);
    }

    @Test
    @DisplayName("Is created by Guice")
    void isCreatedByGuice() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("configure: removeDependencies delegates to PomService.removeDependency for each coord")
    void removeDependenciesDelegatesForEach() {
        when(arguments.getSearchingDirectory()).thenReturn("/repo");
        when(toolConfig.isRecursive()).thenReturn(true);

        when(arguments.hasRemoveDependencies()).thenReturn(true);
        when(arguments.getRemoveDependencies()).thenReturn(List.of("g:a", "x:y:1:test"));

        when(arguments.hasAddDependencies()).thenReturn(false);
        when(arguments.hasChangeDependencies()).thenReturn(false);

        controller.configure();

        Path expectedPath = Path.of("/repo");
        verify(pomService, times(1)).removeDependency(expectedPath, "g:a", true);
        verify(pomService, times(1)).removeDependency(expectedPath, "x:y:1:test", true);

        verify(pomService, never()).addDependency(any(), anyString(), anyBoolean());
        verify(pomService, never()).changeDependency(any(), anyString(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("configure: addDependencies delegates to PomService.addDependency for each coord")
    void addDependenciesDelegatesForEach() {
        when(arguments.getSearchingDirectory()).thenReturn("/repo");
        when(toolConfig.isRecursive()).thenReturn(false);

        when(arguments.hasRemoveDependencies()).thenReturn(false);
        when(arguments.hasAddDependencies()).thenReturn(true);
        when(arguments.getAddDependencies()).thenReturn(List.of("g:a:1:compile", "x:y:2:test"));

        when(arguments.hasChangeDependencies()).thenReturn(false);

        controller.configure();

        Path expectedPath = Path.of("/repo");
        verify(pomService, times(1)).addDependency(expectedPath, "g:a:1:compile", false);
        verify(pomService, times(1)).addDependency(expectedPath, "x:y:2:test", false);

        verify(pomService, never()).removeDependency(any(), anyString(), anyBoolean());
        verify(pomService, never()).changeDependency(any(), anyString(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("configure: changeDependencies delegates once with pair[0], pair[1]")
    void changeDependenciesDelegatesOnce() {
        when(arguments.getSearchingDirectory()).thenReturn("/repo");
        when(toolConfig.isRecursive()).thenReturn(true);

        when(arguments.hasRemoveDependencies()).thenReturn(false);
        when(arguments.hasAddDependencies()).thenReturn(false);
        when(arguments.hasChangeDependencies()).thenReturn(true);
        when(arguments.getChangeDependencies()).thenReturn(List.of("g:a:1:test", "g:a:2:test"));

        controller.configure();

        verify(pomService, times(1)).changeDependency(Path.of("/repo"), "g:a:1:test", "g:a:2:test", true);
        verify(pomService, never()).addDependency(any(), anyString(), anyBoolean());
        verify(pomService, never()).removeDependency(any(), anyString(), anyBoolean());
    }
}
