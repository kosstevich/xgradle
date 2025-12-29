package unittests.parsers;

import com.google.inject.*;

import org.altlinux.xgradle.api.containers.PomContainer;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.bindingannotations.processingtypes.GradlePlugin;
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

public class GradlePluginPomParserTests {

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
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(PomContainer.class).toInstance(pomContainer);
                        bind(Logger.class).toInstance(logger);
                    }
                }
        );

        parser = injector.getInstance(
                Key.get(new TypeLiteral<PomParser<HashMap<String, Path>>>() {}, GradlePlugin.class)
        );
    }

    @Test
    void whenArtifactNamesEmptyReturnsEmptyAndDoesNotCallPomContainer(@TempDir Path tmp) {
        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.empty());
        assertTrue(result.isEmpty());
        verifyNoInteractions(pomContainer);
    }

    @Test
    void whenArtifactNamesPresentUsesGetAllPomsAndFiltersByFilenamePrefix(@TempDir Path tmp) throws Exception {
        Path pluginPom = write(tmp.resolve("plug-1.0.pom"), pluginJarPom("org.example", "plug", "1.0", false));
        Path otherPom = write(tmp.resolve("other-1.0.pom"), pluginJarPom("org.example", "other", "1.0", false));

        Path pluginJar = tmp.resolve("plug-1.0.jar");
        Files.write(pluginJar, new byte[]{0});

        Set<Path> all = new HashSet<Path>(Arrays.asList(pluginPom, otherPom));
        when(pomContainer.getAllPoms(tmp.toString())).thenReturn(all);

        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.of(Arrays.asList("plug")));

        assertEquals(1, result.size());
        assertEquals(pluginJar, result.get(pluginPom.toString()));

        verify(pomContainer, times(1)).getAllPoms(tmp.toString());
        verifyNoMoreInteractions(pomContainer);
    }

    @Test
    void pomPackagingMarkerResolvesDependencyJarAndMapsMarkerPomToDependencyJar(@TempDir Path tmp) throws Exception {
        Path markerPom = write(tmp.resolve("marker-1.0.pom"), markerPomWithJarDependency(
                "org.example", "marker", "1.0",
                "org.dep", "dep", "2.0"
        ));

        Path depJar = tmp.resolve("dep-2.0.jar");
        Files.write(depJar, new byte[]{0});

        when(pomContainer.getAllPoms(tmp.toString())).thenReturn(Collections.singleton(markerPom));

        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.of(Arrays.asList("marker")));

        assertEquals(1, result.size());
        assertEquals(depJar, result.get(markerPom.toString()));
    }

    @Test
    void nonPomPackagingFallsBackToParentGroupIdAndUsesChildVersionForJarName(@TempDir Path tmp) throws Exception {
        Path pom = write(tmp.resolve("pp-1.0.pom"), pluginJarPomWithParentGroupId("pp", "1.0", "org.parent"));
        Path jar = tmp.resolve("pp-1.0.jar");
        Files.write(jar, new byte[]{0});

        when(pomContainer.getAllPoms(tmp.toString())).thenReturn(Collections.singleton(pom));

        HashMap<String, Path> result = parser.getArtifactCoords(tmp.toString(), Optional.of(Arrays.asList("pp")));

        assertEquals(1, result.size());
        assertEquals(jar, result.get(pom.toString()));
    }

    private static Path write(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private static String pluginJarPom(String groupId, String artifactId, String version, boolean withParent) {
        String parent = "";
        if (withParent) {
            parent =
                    "<parent>" +
                            "<groupId>org.parent</groupId>" +
                            "<artifactId>parent</artifactId>" +
                            "<version>9</version>" +
                            "</parent>";
        }

        return ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                + "<modelVersion>4.0.0</modelVersion>"
                + parent
                + "<groupId>" + groupId + "</groupId>"
                + "<artifactId>" + artifactId + "</artifactId>"
                + "<version>" + version + "</version>"
                + "<packaging>jar</packaging>"
                + "<properties><java-gradle-plugin>true</java-gradle-plugin></properties>"
                + "</project>";
    }

    private static String pluginJarPomWithParentGroupId(String artifactId, String version, String parentGroupId) {
        return ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<parent>"
                + "<groupId>" + parentGroupId + "</groupId>"
                + "<artifactId>parent</artifactId>"
                + "<version>9</version>"
                + "</parent>"
                + "<artifactId>" + artifactId + "</artifactId>"
                + "<version>" + version + "</version>"
                + "<packaging>jar</packaging>"
                + "<properties><java-gradle-plugin>true</java-gradle-plugin></properties>"
                + "</project>";
    }

    private static String markerPomWithJarDependency(
            String groupId, String artifactId, String version,
            String depGroupId, String depArtifactId, String depVersion
    ) {
        return ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<groupId>" + groupId + "</groupId>"
                + "<artifactId>" + artifactId + "</artifactId>"
                + "<version>" + version + "</version>"
                + "<packaging>pom</packaging>"
                + "<dependencies>"
                + "<dependency>"
                + "<groupId>" + depGroupId + "</groupId>"
                + "<artifactId>" + depArtifactId + "</artifactId>"
                + "<version>" + depVersion + "</version>"
                + "</dependency>"
                + "</dependencies>"
                + "</project>";
    }
}
