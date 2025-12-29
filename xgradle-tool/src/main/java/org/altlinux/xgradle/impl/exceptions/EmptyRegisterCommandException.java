package org.altlinux.xgradle.impl.exceptions;

public class EmptyRegisterCommandException extends RegistrarException {

    public EmptyRegisterCommandException(String command) {
        super("Register command is empty: '" + command + "'");
    }
}
