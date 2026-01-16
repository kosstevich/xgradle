package org.altlinux.xgradle.impl.enums;

public enum ConfigurationType {
    API("api"),
    IMPLEMENTATION("implementation"),
    RUNTIME("runtime"),
    COMPILE_ONLY("compileOnly"),
    TEST("test"),
    UNKNOWN("implementation");

    private final String gradleConfiguration;

    ConfigurationType(String gradleConfiguration) {
        this.gradleConfiguration = gradleConfiguration;
    }

    public String gradleConfiguration() {
        return gradleConfiguration;
    }
}
