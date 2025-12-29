package unittests.parsers;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import org.altlinux.xgradle.api.caches.ArtifactCache;
import org.altlinux.xgradle.api.containers.PomContainer;
import org.altlinux.xgradle.api.model.ArtifactCoordinates;
import org.altlinux.xgradle.api.model.ArtifactData;
import org.altlinux.xgradle.api.model.ArtifactFactory;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Library;
import org.altlinux.xgradle.impl.parsers.ParsersModule;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PomParser<HashMap<String,Path>> contract (Library parser)")
class LibraryPomParserTests {

    @Mock private PomContainer pomContainer;
    @Mock private ArtifactCache artifactCache;
    @Mock private ArtifactFactory artifactFactory;
    @Mock private Logger logger;

    private PomParser<HashMap<String, Path>> parser;

    @BeforeEach
    void setUp() {
        Injector injector = Guice.createInjector(
                Modules.override(new ParsersModule()).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(PomContainer.class).toInstance(pomContainer);
                        bind(ArtifactCache.class).toInstance(artifactCache);
                        bind(ArtifactFactory.class).toInstance(artifactFactory);
                        bind(Logger.class).toInstance(logger);
                    }
                })
        );

        parser = injector.getInstance(
                Key.get(new TypeLiteral<PomParser<HashMap<String, Path>>>() {}, Library.class)
        );

        when(artifactCache.add(any())).thenReturn(true);

        when(artifactFactory.coordinates(anyString(), anyString(), anyString()))
                .thenAnswer(inv -> mock(ArtifactCoordinates.class));

        when(artifactFactory.data(any(ArtifactCoordinates.class), any(Model.class), any(Path.class), any(Path.class)))
                .thenAnswer(inv -> {
                    ArtifactCoordinates coords = inv.getArgument(0);
                    Path pomPath = inv.getArgument(2);
                    Path jarPath = inv.getArgument(3);

                    ArtifactData data = mock(ArtifactData.class);
                    when(data.getCoordinates()).thenReturn(coords);
                    when(data.getPomPath()).thenReturn(pomPath);
                    when(data.getJarPath()).thenReturn(jarPath);
                    return data;
                });
    }

    @Test
    @DisplayName("whenArtifactNamesPresentUsesGetSelectedPoms")
    void whenArtifactNamesPresentUsesGetSelectedPoms(@TempDir Path dir) throws Exception {
        Path pom = writePom(dir.resolve("lib-1.0.pom"), pomXml("org.example", "lib", "1.0"));
        Path jar = touch(dir.resolve("lib-1.0.jar"));

        List<String> names = List.of("lib");
        when(pomContainer.getSelectedPoms(dir.toString(), names)).thenReturn(Set.of(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(dir.toString(), Optional.of(names));

        verify(pomContainer, times(1)).getSelectedPoms(dir.toString(), names);
        verify(pomContainer, never()).getAllPoms(anyString());

        assertEquals(1, result.size());
        assertEquals(jar, result.get(pom.toString()));
    }

    @Test
    @DisplayName("whenArtifactNamesEmptyUsesGetAllPoms")
    void whenArtifactNamesEmptyUsesGetAllPoms(@TempDir Path dir) throws Exception {
        Path pom = writePom(dir.resolve("lib-1.0.pom"), pomXml("org.example", "lib", "1.0"));
        Path jar = touch(dir.resolve("lib-1.0.jar"));

        when(pomContainer.getAllPoms(dir.toString())).thenReturn(Set.of(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(dir.toString(), Optional.empty());

        verify(pomContainer, times(1)).getAllPoms(dir.toString());
        verify(pomContainer, never()).getSelectedPoms(anyString(), anyList());

        assertEquals(1, result.size());
        assertEquals(jar, result.get(pom.toString()));
    }

    @Test
    @DisplayName("fallsBackToParentForGroupIdAndVersion")
    void fallsBackToParentForGroupIdAndVersion(@TempDir Path dir) throws Exception {
        Path pom = writePom(dir.resolve("child.pom"), pomXmlWithParent(
                "org.parent", "parent", "2.5",
                "child"
        ));
        Path jar = touch(dir.resolve("child-2.5.jar"));

        when(pomContainer.getAllPoms(dir.toString())).thenReturn(Set.of(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(dir.toString(), Optional.empty());

        assertEquals(1, result.size());
        assertEquals(jar, result.get(pom.toString()));
    }

    @Test
    @DisplayName("skipsDuplicateWhenSameCoordinatesSeenTwice")
    void skipsDuplicateWhenSameCoordinatesSeenTwice(@TempDir Path dir) throws Exception {
        Path aPom = writePom(dir.resolve("a.pom"), pomXml("org.example", "dup", "1.0"));
        Path bPom = writePom(dir.resolve("b.pom"), pomXml("org.example", "dup", "1.0"));
        Path jar = touch(dir.resolve("dup-1.0.jar"));

        when(pomContainer.getAllPoms(dir.toString())).thenReturn(Set.of(aPom, bPom));

        when(artifactCache.add(any())).thenReturn(true, false);

        ArtifactData existing = mock(ArtifactData.class);
        when(existing.getPomPath()).thenReturn(Path.of("/tmp/existing.pom"));
        when(artifactCache.get(any())).thenReturn(existing);

        HashMap<String, Path> result = parser.getArtifactCoords(dir.toString(), Optional.empty());

        assertEquals(1, result.size());

        String onlyKey = result.keySet().iterator().next();
        assertTrue(onlyKey.equals(aPom.toString()) || onlyKey.equals(bPom.toString()));
        assertEquals(jar, result.get(onlyKey));

        verify(artifactCache, times(2)).add(any());
        verify(artifactCache, times(1)).get(any());
    }

    private static Path touch(Path p) throws IOException {
        if (Files.notExists(p)) {
            Files.createFile(p);
        }
        return p;
    }

    private static Path writePom(Path pomPath, String xml) throws IOException {
        Files.write(pomPath, xml.getBytes(StandardCharsets.UTF_8));
        return pomPath;
    }

    private static String pomXml(String groupId, String artifactId, String version) {
        return String.format(
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">" +
                        "<modelVersion>4.0.0</modelVersion>" +
                        "<groupId>%s</groupId>" +
                        "<artifactId>%s</artifactId>" +
                        "<version>%s</version>" +
                        "</project>",
                groupId, artifactId, version
        );
    }

    private static String pomXmlWithParent(
            String parentGroupId, String parentArtifactId, String parentVersion,
            String childArtifactId
    ) {
        return String.format(
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">" +
                        "<modelVersion>4.0.0</modelVersion>" +
                        "<parent>" +
                        "<groupId>%s</groupId>" +
                        "<artifactId>%s</artifactId>" +
                        "<version>%s</version>" +
                        "</parent>" +
                        "<artifactId>%s</artifactId>" +
                        "</project>",
                parentGroupId, parentArtifactId, parentVersion, childArtifactId
        );
    }
}
