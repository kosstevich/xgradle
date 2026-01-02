package org.altlinux.xgradle.impl.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class DependencySpec {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String scope;

    private DependencySpec(String groupId, String artifactId, String version, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
    }

    public static DependencySpec parse(String coords) {
        Objects.requireNonNull(coords, "coords");

        String trimmed = coords.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("coords is empty");
        }

        String[] parts = trimmed.split(":", -1);
        if (parts.length < 2 || parts.length > 4) {
            throw new IllegalArgumentException("Invalid coords format. Expected groupId:artifactId[:version[:scope]]");
        }

        String g = parts[0].trim();
        String a = parts[1].trim();
        if (g.isEmpty() || a.isEmpty()) {
            throw new IllegalArgumentException("groupId and artifactId must be non-empty");
        }

        String v = parts.length >= 3 ? normalizeOpt(parts[2]) : null;
        String s = parts.length == 4 ? normalizeOpt(parts[3]) : null;

        return new DependencySpec(g, a, v, s);
    }

    private static String normalizeOpt(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    public Optional<String> getScope() {
        return Optional.ofNullable(scope);
    }

    @Override
    public String toString() {
        return String.join(":", Arrays.asList(
                groupId,
                artifactId,
                version == null ? "" : version,
                scope == null ? "" : scope
        ));
    }
}
