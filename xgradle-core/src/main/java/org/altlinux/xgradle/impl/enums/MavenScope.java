package org.altlinux.xgradle.impl.enums;

/**
 * Maven dependency scope.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
public enum MavenScope {

    COMPILE("compile"),

    RUNTIME("runtime"),

    PROVIDED("provided"),

    TEST("test");

    private final String scope;

    MavenScope(String scope) {
        this.scope = scope;
    }

    /**
     * Returns Maven scope string value.
     *
     * @return scope value
     */
    public String getScope() {
        return scope;
    }

    /**
     * Converts raw scope string to enum.
     *
     * @param value raw scope value from POM (may be null/blank)
     * @return parsed scope or COMPILE if value is missing/unknown
     */
    public static MavenScope fromScope(String value) {
        if (value == null || value.isBlank()) {
            return COMPILE;
        }

        String normalized = value.trim().toLowerCase();
        for (MavenScope s : values()) {
            if (s.scope.equals(normalized)) {
                return s;
            }
        }
        return COMPILE;
    }
}
