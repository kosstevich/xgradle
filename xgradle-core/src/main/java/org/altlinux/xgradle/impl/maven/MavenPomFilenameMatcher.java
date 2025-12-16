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
package org.altlinux.xgradle.impl.maven;

import org.gradle.util.internal.VersionNumber;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper for matching POM filenames to Maven artifacts and versions.
 *
 * <p>This class encapsulates the logic of generating filename variants
 * and deciding whether a given filename represents a valid versioned
 * artifact name.</p>
 *
 * <p>Version detection is based on Gradle {@link VersionNumber} instead
 * of regular expressions to keep behaviour consistent with Gradle
 * version handling.</p>
 *
 * @author Ivan Khanas
 */
public class MavenPomFilenameMatcher {

    /**
     * Checks whether the given path corresponds to a candidate POM file
     * for the specified artifact.
     *
     * <p>The following patterns are supported:</p>
     * <ul>
     *   <li>{@code <variant>.pom}</li>
     *   <li>{@code <variant>-&lt;version&gt;.pom}</li>
     *   <li>{@code <variant>-&lt;version&gt;-&lt;artifactId&gt;.pom}</li>
     * </ul>
     *
     * @param path       candidate POM file path
     * @param variant    base name variant (e.g. {@code commons-lang3})
     * @param artifactId Maven artifact ID
     *
     * @return {@code true} if filename matches one of the supported patterns,
     *         {@code false} otherwise
     */
    boolean matches(Path path, String variant, String artifactId) {
        String fileName = path.getFileName().toString();
        if (!fileName.endsWith(".pom")) {
            return false;
        }

        String baseName = fileName.substring(0, fileName.length() - 4);

        if (baseName.equals(variant)) {
            return true;
        }

        if (baseName.startsWith(variant + "-")) {
            String suffix = baseName.substring(variant.length() + 1);
            return isVersionString(suffix) || isVersionedArtifact(suffix, artifactId);
        }

        return false;
    }

    /**
     * Generates possible base-name variants for an artifact based on
     * its group ID and artifact ID.
     *
     * <p>Examples for {@code org.apache.commons:commons-lang3}:</p>
     * <ul>
     *   <li>{@code commons-lang3}</li>
     *   <li>{@code apache-commons-commons-lang3}</li>
     *   <li>{@code apache-commons-lang3}</li>
     * </ul>
     *
     * @param groupId    Maven group ID
     * @param artifactId Maven artifact ID
     *
     * @return set of possible filename base variants
     */
    Set<String> generateNameVariants(String groupId, String artifactId) {
        Set<String> variants = new HashSet<>();
        variants.add(artifactId);

        String[] groupParts = groupId.split("\\.");

        if (groupParts.length > 1) {
            String groupSuffix = String.join("-", Arrays.copyOfRange(groupParts, 1, groupParts.length));
            variants.add(groupSuffix + "-" + artifactId);

            if (groupParts.length > 2) {
                variants.add(groupParts[1] + "-" + artifactId);
            }
        }

        return variants;
    }

    /**
     * Checks if the given suffix has the form {@code <version>-<artifactId>}
     * where {@code <version>} is a valid version string.
     *
     * @param suffix     filename suffix to analyse
     * @param artifactId Maven artifact ID
     *
     * @return {@code true} if suffix is a versioned artifact name,
     *         {@code false} otherwise
     */
    private boolean isVersionedArtifact(String suffix, String artifactId) {
        if (!suffix.endsWith(artifactId)) {
            return false;
        }

        if (suffix.length() <= artifactId.length() + 1) {
            return false;
        }

        String versionPart = suffix.substring(
                0,
                suffix.length() - artifactId.length() - 1
        );

        return isVersionString(versionPart);
    }

    /**
     * Determines whether the given string represents a valid version.
     *
     * <p>Behaviour is intentionally aligned with Gradle's
     * {@link VersionNumber#parse(String)}:</p>
     * <ul>
     *   <li>string must be non-empty,</li>
     *   <li>first character must be a digit,</li>
     *   <li>parsed version must not be {@link VersionNumber#UNKNOWN}.</li>
     * </ul>
     *
     * @param str version candidate string
     *
     * @return {@code true} if the string looks like a valid version,
     *         {@code false} otherwise
     */
    private boolean isVersionString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        if (!Character.isDigit(str.charAt(0))) {
            return false;
        }

        try {
            VersionNumber version = VersionNumber.parse(str);
            return !VersionNumber.UNKNOWN.equals(version);
        } catch (Exception e) {
            return false;
        }
    }
}
