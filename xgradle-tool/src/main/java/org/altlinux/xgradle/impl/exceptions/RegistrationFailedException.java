package org.altlinux.xgradle.impl.exceptions;

import java.util.List;

public class RegistrationFailedException extends RuntimeException {
    public RegistrationFailedException(List<String> command, int exitCode) {
        super("Failed to register artifact, exit code: "
                + exitCode +
                ", command: " + String.join(" ", command));
    }
}
