package org.altlinux.xgradle.api.managers;

@FunctionalInterface
public interface Manager<I> {

    void configure(I input);
}
