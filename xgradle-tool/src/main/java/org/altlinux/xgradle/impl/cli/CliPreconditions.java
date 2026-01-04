package org.altlinux.xgradle.impl.cli;

import org.altlinux.xgradle.impl.exceptions.CliUsageException;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Utility class containing precondition checks for CLI execution.
 * Throws {@link CliUsageException} when required conditions are not met.
 *
 * @author Ivan Khanas
 */
public final class CliPreconditions {

    private CliPreconditions() {
    }

    /**
     * Ensures the given condition is true.
     *
     * @param condition condition to check
     * @param message error message to use for {@link CliUsageException}
     * @throws CliUsageException when condition is false
     */
    public static void require(boolean condition, String message) {
        Objects.requireNonNull(message, "message");
        if (!condition) {
            throw new CliUsageException(message);
        }
    }

    /**
     * Ensures the given condition is true.
     * This overload allows lazy message construction.
     *
     * @param condition condition to check
     * @param messageSupplier supplies error message for {@link CliUsageException}
     * @throws CliUsageException when condition is false
     */
    public static void require(boolean condition, Supplier<String> messageSupplier) {
        Objects.requireNonNull(messageSupplier, "messageSupplier");
        if (!condition) {
            throw new CliUsageException(Objects.requireNonNull(messageSupplier.get(), "message"));
        }
    }
}
