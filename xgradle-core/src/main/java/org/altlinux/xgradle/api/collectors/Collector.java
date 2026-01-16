package org.altlinux.xgradle.api.collectors;

/**
 * Generic collector contract.
 *
 * @param <I> input type
 * @param <O> output type
 */
public interface Collector<I, O> {

    /**
     * Collects information from the input.
     *
     * @param input input object
     * @return collected result
     */
    O collect(I input);
}
