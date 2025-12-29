/*
 * Copyright 2025 BaseALT Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unittests.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.altlinux.xgradle.impl.cli.CliArgumentsContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test arguments container")
public class CliArgumentsContainerTest {

    private static CliArgumentsContainer parse(String... argv) {
        CliArgumentsContainer args = new CliArgumentsContainer();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        return args;
    }

    private static CliArgumentsContainer parseAndValidate(String... argv) {
        CliArgumentsContainer args = parse(argv);
        args.validateMutuallyExclusive();
        return args;
    }

    @Test
    @DisplayName("Defaults: no args -> all optionals are empty, all flags are false, all has* are false")
    void defaults_noArgs() {
        CliArgumentsContainer args = parse();

        assertAll(
                () -> assertFalse(args.hasXmvnRegister()),
                () -> assertFalse(args.hasSearchingDirectory()),
                () -> assertFalse(args.hasInstallPrefix()),
                () -> assertFalse(args.hasInstallPluginParameter()),
                () -> assertFalse(args.hasBomRegistration()),
                () -> assertFalse(args.hasJavadocRegistration()),
                () -> assertFalse(args.hasJarInstallationDirectory()),
                () -> assertFalse(args.hasPomInstallationDirectory()),
                () -> assertFalse(args.hasAllowSnapshots()),
                () -> assertFalse(args.hasRemoveParentPoms()),
                () -> assertFalse(args.hasVersion()),
                () -> assertFalse(args.hasHelp()),
                () -> assertFalse(args.hasArtifactName()),

                () -> assertTrue(args.getArtifactName().isEmpty()),
                () -> assertNull(args.getExcludedArtifact()),
                () -> assertNull(args.getRemoveParentPoms()),
                () -> assertNull(args.getXmvnRegister()),
                () -> assertNull(args.getSearchingDirectory()),
                () -> assertNull(args.getInstallPrefix()),
                () -> assertNull(args.getJarInstallationDirectory()),
                () -> assertNull(args.getPomInstallationDirectory())
        );
    }

    @Test
    @DisplayName("JCommander: parses --xmvn-register and --searching-directory and reflects in has/get")
    void parses_xmvn_register_command() {
        CliArgumentsContainer args = parseAndValidate(
                "--xmvn-register=/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py",
                "--searching-directory=/home/xeno/.m2"
        );

        assertAll(
                () -> assertTrue(args.hasXmvnRegister()),
                () -> assertEquals("/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py", args.getXmvnRegister()),
                () -> assertTrue(args.hasSearchingDirectory()),
                () -> assertEquals("/home/xeno/.m2", args.getSearchingDirectory()),
                () -> assertFalse(args.hasBomRegistration()),
                () -> assertFalse(args.hasJavadocRegistration()),
                () -> assertFalse(args.hasInstallPluginParameter())
        );
    }

    @Test
    @DisplayName("Validation: OK when only --xmvn-register is provided")
    void validateMutuallyExclusive_ok_whenOnlyXmvnRegister() {
        CliArgumentsContainer args = parse(
                "--xmvn-register=/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py"
        );
        assertDoesNotThrow(args::validateMutuallyExclusive);
    }

    @Test
    @DisplayName("Validation: throws when --xmvn-register and --install-gradle-plugin are used together")
    void validateMutuallyExclusive_throws_whenXmvnRegisterAndInstallPluginTogether() {
        CliArgumentsContainer args = parse(
                "--xmvn-register=/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py",
                "--install-gradle-plugin"
        );

        ParameterException ex = assertThrows(ParameterException.class, args::validateMutuallyExclusive);
        assertAll(
                () -> assertTrue(ex.getMessage().contains("--xmvn-register and --install-gradle-plugin")),
                () -> assertTrue(ex.getMessage().contains("Use only one main mode at a time"))
        );
    }

    @Test
    @DisplayName("JCommander: parses --register-bom + --xmvn-register + --searching-directory")
    void parses_register_bom_command() {
        CliArgumentsContainer args = parseAndValidate(
                "--xmvn-register=/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py",
                "--register-bom",
                "--searching-directory=/home/xeno/.m2"
        );

        assertAll(
                () -> assertTrue(args.hasXmvnRegister()),
                () -> assertTrue(args.hasBomRegistration()),
                () -> assertTrue(args.hasSearchingDirectory()),
                () -> assertFalse(args.hasJavadocRegistration()),
                () -> assertFalse(args.hasInstallPluginParameter())
        );
    }

    @Test
    @DisplayName("JCommander: parses --register-javadoc with required paths (search dir, install prefix, jar dir)")
    void parses_register_javadoc_command() {
        CliArgumentsContainer args = parseAndValidate(
                "--register-javadoc",
                "--searching-directory=/home/xeno/.m2",
                "--install-prefix=/buildroot",
                "--jar-installation-dir=/buildroot/usr/share/javadoc/xgradle"
        );

        assertAll(
                () -> assertTrue(args.hasJavadocRegistration()),
                () -> assertTrue(args.hasSearchingDirectory()),
                () -> assertEquals("/home/xeno/.m2", args.getSearchingDirectory()),
                () -> assertTrue(args.hasInstallPrefix()),
                () -> assertEquals("/buildroot", args.getInstallPrefix()),
                () -> assertTrue(args.hasJarInstallationDirectory()),
                () -> assertEquals("/buildroot/usr/share/javadoc/xgradle", args.getJarInstallationDirectory()),
                () -> assertFalse(args.hasBomRegistration()),
                () -> assertFalse(args.hasXmvnRegister()),
                () -> assertFalse(args.hasInstallPluginParameter())
        );
    }

    @Test
    @DisplayName("JCommander: parses --install-gradle-plugin with jar/pom installation dirs")
    void parses_install_gradle_plugin_command() {
        CliArgumentsContainer args = parseAndValidate(
                "--install-gradle-plugin",
                "--searching-directory=/home/xeno/.m2",
                "--jar-installation-dir=/buildroot/usr/share/java/xgradle",
                "--pom-installation-dir=/buildroot/usr/share/maven-poms/xgradle"
        );

        assertAll(
                () -> assertTrue(args.hasInstallPluginParameter()),
                () -> assertTrue(args.hasSearchingDirectory()),
                () -> assertTrue(args.hasJarInstallationDirectory()),
                () -> assertTrue(args.hasPomInstallationDirectory()),
                () -> assertEquals("/buildroot/usr/share/java/xgradle", args.getJarInstallationDirectory()),
                () -> assertEquals("/buildroot/usr/share/maven-poms/xgradle", args.getPomInstallationDirectory()),
                () -> assertFalse(args.hasXmvnRegister()),
                () -> assertFalse(args.hasBomRegistration()),
                () -> assertFalse(args.hasJavadocRegistration())
        );
    }

    @Test
    @DisplayName("JCommander: parses --artifacts as a list when passed multiple values")
    void parses_artifacts_list() {
        CliArgumentsContainer args = parse("--artifacts=a", "--artifacts=b", "--artifacts=c");

        assertAll(
                () -> assertTrue(args.hasArtifactName()),
                () -> assertEquals(List.of("a", "b", "c"), args.getArtifactName().orElseThrow())
        );
    }

    @Test
    @DisplayName("JCommander: parses --exclude-artifacts as a list when passed multiple values")
    void parses_exclude_artifacts_list() {
        CliArgumentsContainer args = parse("--exclude-artifacts=x", "--exclude-artifacts=y");

        assertAll(
                () -> assertNotNull(args.getExcludedArtifact()),
                () -> assertEquals(List.of("x", "y"), args.getExcludedArtifact())
        );
    }

    @Test
    @DisplayName("JCommander: parses --allow-snapshots flag")
    void parses_allow_snapshots_flag() {
        CliArgumentsContainer args = parse("--allow-snapshots");
        assertTrue(args.hasAllowSnapshots());
    }

    @Test
    @DisplayName("JCommander: parses --remove-parent as a list when passed multiple values")
    void parses_remove_parent_list() {
        CliArgumentsContainer args = parse("--remove-parent=p1", "--remove-parent=p2");

        assertAll(
                () -> assertTrue(args.hasRemoveParentPoms()),
                () -> assertEquals(List.of("p1", "p2"), args.getRemoveParentPoms())
        );
    }

    @Test
    @DisplayName("JCommander: parses --help and --version flags")
    void parses_help_and_version_flags() {
        CliArgumentsContainer args = parse("--help", "--version");

        assertAll(
                () -> assertTrue(args.hasHelp()),
                () -> assertTrue(args.hasVersion())
        );
    }

    @Test
    @DisplayName("Validation: throws when --install-gradle-plugin and --register-bom are used together")
    void validateMutuallyExclusive_throws_installPlugin_with_registerBom() {
        CliArgumentsContainer args = parse("--install-gradle-plugin", "--register-bom");

        ParameterException ex = assertThrows(ParameterException.class, args::validateMutuallyExclusive);
        assertTrue(ex.getMessage().contains("--install-gradle-plugin and --register-bom"));
    }

    @Test
    @DisplayName("Validation: throws when --register-javadoc and --install-gradle-plugin are used together")
    void validateMutuallyExclusive_throws_registerJavadoc_with_installPlugin() {
        CliArgumentsContainer args = parse("--register-javadoc", "--install-gradle-plugin");

        ParameterException ex = assertThrows(ParameterException.class, args::validateMutuallyExclusive);
        assertTrue(ex.getMessage().contains("--register-javadoc and --install-gradle-plugin"));
    }

    @Test
    @DisplayName("Validation: throws when --register-javadoc and --register-bom are used together")
    void validateMutuallyExclusive_throws_registerJavadoc_with_registerBom() {
        CliArgumentsContainer args = parse("--register-javadoc", "--register-bom");

        ParameterException ex = assertThrows(ParameterException.class, args::validateMutuallyExclusive);
        assertTrue(ex.getMessage().contains("--register-javadoc and --register-bom"));
    }

    @Test
    @DisplayName("Validation: throws when --register-javadoc and --xmvn-register are used together")
    void validateMutuallyExclusive_throws_registerJavadoc_with_xmvnRegister() {
        CliArgumentsContainer args = parse(
                "--register-javadoc",
                "--xmvn-register=/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py"
        );

        ParameterException ex = assertThrows(ParameterException.class, args::validateMutuallyExclusive);
        assertTrue(ex.getMessage().contains("--register-javadoc and --xmvn-register"));
    }

    @Test
    @DisplayName("JCommander: unknown option triggers ParameterException")
    void unknown_option_throws() {
        assertThrows(ParameterException.class, () -> parse("--no-such-flag"));
    }

    @Test
    @DisplayName("JCommander: missing value for string option triggers ParameterException")
    void missing_value_for_string_option_throws() {
        assertThrows(ParameterException.class, () -> parse("--searching-directory"));
    }

    @Test
    @DisplayName("JCommander: --xmvn-register supports spaces when passed as a single argv token with '='")
    void xmvn_register_supports_spaces_in_value() {
        CliArgumentsContainer args = parseAndValidate(
                "--xmvn-register=/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py"
        );
        assertEquals("/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py", args.getXmvnRegister());
    }
}