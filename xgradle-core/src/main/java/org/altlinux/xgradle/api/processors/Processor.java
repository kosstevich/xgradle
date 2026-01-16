package org.altlinux.xgradle.api.processors;

@FunctionalInterface
public interface Processor<I> {

    void process(I input);
}
