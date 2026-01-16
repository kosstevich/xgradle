package org.altlinux.xgradle.api.handlers;

@FunctionalInterface
public interface Handler<I> {
    
    void handle(I input);
}
