package unittests.redactors;

import com.google.inject.*;
import com.google.inject.util.Modules;

import org.altlinux.xgradle.api.redactors.ParentRedactor;
import org.altlinux.xgradle.impl.bindingannotations.pomprocessingoperations.Remove;
import org.altlinux.xgradle.impl.redactors.RedactorsModule;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.slf4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("ParentRemover contract")
public class ParentRemoverTests {

    @TempDir
    Path tmp;

    @Mock
    Logger logger;

    private ParentRedactor parentRedactor;

    @BeforeEach
    void setUp() {
        Injector injector = Guice.createInjector(
                Modules.override(new RedactorsModule())
                        .with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(Logger.class).toInstance(logger);
                        }
                        })
        );
        parentRedactor = injector.getInstance(Key.get(ParentRedactor.class, Remove.class));
    }

    @Test
    @DisplayName("Remove <parent> block and rewrites POM")
    void removeParentBlock() throws IOException, XmlPullParserException {
        Path pomPath = tmp.resolve("pom.xml");

        Files.writeString(pomPath, pomWithParent());

        parentRedactor.removeParent(pomPath);

        Model model = new MavenXpp3Reader().read(new FileReader(pomPath.toFile()));
        assertNull(model.getParent());

        String afterRemoving = Files.readString(pomPath);
        assertFalse(afterRemoving.contains("<parent>"));

        verify(logger, never()).warn(anyString());
        verifyNoMoreInteractions(logger);

    }

    @Test
    @DisplayName("If POM has no <parent>: logs warn and does not modify file")
    public void warnsIfNoParent() throws IOException {
        Path pomPath = tmp.resolve("pom.xml");
        Files.writeString(pomPath, pomWithoutParent());

        String beforeRemoving = Files.readString(pomPath);

        parentRedactor.removeParent(pomPath);

        String afterRemoving = Files.readString(pomPath);

        assertEquals(beforeRemoving, afterRemoving);

        verify(logger).warn("POM file hasn`t parent block, cannot remove: " + pomPath);
        verifyNoMoreInteractions(logger);
    }

    private static String pomWithParent() {
        return ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                + "  <modelVersion>4.0.0</modelVersion>\n"
                + "  <parent>\n"
                + "    <groupId>org.parent</groupId>\n"
                + "    <artifactId>parent</artifactId>\n"
                + "    <version>1</version>\n"
                + "  </parent>\n"
                + "  <groupId>org.example</groupId>\n"
                + "  <artifactId>child</artifactId>\n"
                + "  <version>1</version>\n"
                + "</project>\n";
    }

    private static String pomWithoutParent() {
        return ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                + "  <modelVersion>4.0.0</modelVersion>\n"
                + "  <groupId>org.example</groupId>\n"
                + "  <artifactId>child</artifactId>\n"
                + "  <version>1</version>\n"
                + "</project>\n";
    }
}
