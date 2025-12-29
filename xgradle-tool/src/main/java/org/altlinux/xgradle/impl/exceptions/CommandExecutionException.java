package org.altlinux.xgradle.impl.exceptions;

import java.util.List;

public class CommandExecutionException extends RegistrarException {

    public CommandExecutionException(List<String> command, Throwable cause) {
        super("Failed to execute command: " + String.join(" ", command), cause);
    }
}
