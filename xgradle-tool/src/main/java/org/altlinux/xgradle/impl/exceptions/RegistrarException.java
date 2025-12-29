package org.altlinux.xgradle.impl.exceptions;

public class RegistrarException extends RuntimeException {

    public RegistrarException(String message) {
        super(message);
    }

    public RegistrarException(String message, Throwable cause) {
        super(message, cause);
    }
}
