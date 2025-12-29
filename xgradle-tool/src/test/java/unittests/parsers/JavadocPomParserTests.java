package unittests.parsers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.altlinux.xgradle.api.containers.PomContainer;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.Javadoc;
import org.altlinux.xgradle.impl.caches.CachesModule;
import org.altlinux.xgradle.impl.model.ModelModule;
import org.altlinux.xgradle.impl.parsers.ParsersModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JavadocPomParserTests {

    private PomContainer pomContainer;
    private PomParser<HashMap<String, Path>> parser;

    @BeforeEach
    void setUp() {
        pomContainer = mock(PomContainer.class);
        Logger logger = LoggerFactory.getLogger("test");

        Injector injector = Guice.createInjector(
                new ParsersModule(),
                new ModelModule(),
                new CachesModule(),
                new com.google.inject.AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(PomContainer.class).toInstance(pomContainer);
                        bind(Logger.class).toInstance(logger);
                    }
                }
        );

        parser = injector.getInstance(
                Key.get(new TypeLiteral<PomParser<HashMap<String, Path>>>() {}, Javadoc.class)
        );
    }

    @Test
    void includesEntryWhenJavadocJarExistsNextToPom(@TempDir Path tmp) throws Exception {
        Path pom = write(tmp.resolve("lib-1.0.pom"), simplePom("org.example", "lib", "1.0"));
        Path javadoc = tmp.resolve("lib-1.0-javadoc.jar");
        Files.write(javadoc, new byte[]{0});

        when(pomContainer.getAllPoms(tmp.toString())).thenReturn(Collections.singleton(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.empty());

        assertEquals(1, result.size());
        assertEquals(javadoc, result.get(pom.toString()));
    }

    @Test
    void skipsEntryWhenJavadocJarDoesNotExist(@TempDir Path tmp) throws Exception {
        Path pom = write(tmp.resolve("lib-1.0.pom"), simplePom("org.example", "lib", "1.0"));
        when(pomContainer.getAllPoms(tmp.toString())).thenReturn(Collections.singleton(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.empty());

        assertTrue(result.isEmpty());
    }

    @Test
    void fallsBackToParentForVersionAndResolvesJarNameAccordingly(@TempDir Path tmp) throws Exception {
        Path pom = write(tmp.resolve("child.pom"), pomWithParentVersion("org.example", "child", "9.9"));
        Path javadoc = tmp.resolve("child-9.9-javadoc.jar");
        Files.write(javadoc, new byte[]{0});

        when(pomContainer.getAllPoms(tmp.toString())).thenReturn(Collections.singleton(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.empty());

        assertEquals(1, result.size());
        assertEquals(javadoc, result.get(pom.toString()));
    }

    @Test
    void whenArtifactNamesPresentUsesGetSelectedPoms(@TempDir Path tmp) throws Exception {
        Path pom = write(tmp.resolve("aa-1.0.pom"), simplePom("org.example", "aa", "1.0"));
        Path javadoc = tmp.resolve("aa-1.0-javadoc.jar");
        Files.write(javadoc, new byte[]{0});

        List<String> names = Arrays.asList("aa");
        when(pomContainer.getSelectedPoms(tmp.toString(), names)).thenReturn(Collections.singleton(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.of(names));

        assertEquals(1, result.size());
        assertEquals(javadoc, result.get(pom.toString()));
        verify(pomContainer, times(1)).getSelectedPoms(tmp.toString(), names);
        verifyNoMoreInteractions(pomContainer);
    }

    private static Path write(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private static String simplePom(String groupId, String artifactId, String version) {
        return ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<groupId>" + groupId + "</groupId>"
                + "<artifactId>" + artifactId + "</artifactId>"
                + "<version>" + version + "</version>"
                + "</project>";
    }

    private static String pomWithParentVersion(String groupId, String artifactId, String parentVersion) {
        return ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<parent>"
                + "<groupId>" + groupId + "</groupId>"
                + "<artifactId>parent</artifactId>"
                + "<version>" + parentVersion + "</version>"
                + "</parent>"
                + "<groupId>" + groupId + "</groupId>"
                + "<artifactId>" + artifactId + "</artifactId>"
                + "</project>";
    }
}
