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
package org.altlinux.xgradle.impl.cli;

import com.google.inject.Singleton;

import org.altlinux.xgradle.api.cli.CommandLineParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link CommandLineParser}.
 * Parses a shell-like command string into argv-style list.
 *
 * @author Ivan Khanas
 */
@Singleton
class DefaultCommandLineParser implements CommandLineParser {

    /**
     * Parses a command string into executable parts.
     * Supports single and double quotes. Quote characters are not included in the result.
     *
     * @param command the command string to parse
     * @return list of parsed arguments; empty list if {@code command} is {@code null} or blank
     * @throws IllegalArgumentException if the command contains an unclosed quote
     */
    @Override
    public List<String> parseCommandLine(String command) {
        if (command == null || command.isBlank()) {
            return List.of();
        }

        String input = stripWrappingQuotes(command.trim());

        List<String> parts = new ArrayList<>();
        StringBuilder token = new StringBuilder();

        boolean inQuotes = false;
        char quoteChar = 0;
        boolean escaping = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escaping) {
                token.append(c);
                escaping = false;
                continue;
            }

            if (inQuotes && c == '\\') {
                escaping = true;
                continue;
            }

            if (!inQuotes && (c == '"' || c == '\'')) {
                inQuotes = true;
                quoteChar = c;
                continue;
            }

            if (inQuotes && c == quoteChar) {
                inQuotes = false;
                quoteChar = 0;
                continue;
            }

            if (!inQuotes && Character.isWhitespace(c)) {
                flush(parts, token);
                continue;
            }

            token.append(c);
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Unclosed quote in command: " + command);
        }

        flush(parts, token);
        return parts;
    }

    private static void flush(List<String> parts, StringBuilder token) {
        if (token.length() == 0) {
            return;
        }
        parts.add(token.toString());
        token.setLength(0);
    }

    private static String stripWrappingQuotes(String s) {
        if (s.length() < 2) {
            return s;
        }
        char first = s.charAt(0);
        char last = s.charAt(s.length() - 1);

        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
