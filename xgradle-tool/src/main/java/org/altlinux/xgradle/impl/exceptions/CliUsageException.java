/*
 * Copyright 2025 BaseALT Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.altlinux.xgradle.impl.exceptions;

/**
 * Exception for invalid CLI usage.
 * Used to signal that required arguments are missing or inconsistent.
 * Intended to be caught at the application boundary to print usage and return error exit code.
 *
 * @author Ivan Khanas
 */
public final class CliUsageException extends RuntimeException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message human-readable description of the CLI usage error
     */
    public CliUsageException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message human-readable description of the CLI usage error
     * @param cause the underlying cause
     */
    public CliUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
