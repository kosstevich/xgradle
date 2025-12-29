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
package unittests.cli;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.altlinux.xgradle.api.cli.CommandExecutor;
import org.altlinux.xgradle.impl.cli.CliModule;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Test command execution")
class CommandLineExecutorTests {

    private CommandExecutor executor;

    @BeforeEach
    void setUp() {
        Injector injector = Guice.createInjector(new CliModule());
        executor = injector.getInstance(CommandExecutor.class);
    }

    /**
     * Ensures the executor returns the exact process exit code.
     */
    @Test
    @DisplayName("Returns process exit code")
    void returnsExitCode() throws Exception {
        assumePosixShellAvailable();

        int code = executor.execute(new ProcessBuilder("sh", "-c", "exit 7"));
        assertEquals(7, code);
    }

    /**
     * Ensures stdout lines are streamed to System.out.
     */
    @Test
    @DisplayName("Streams stdout to System.out")
    void streamsStdoutToSystemOut() throws Exception {
        assumePosixShellAvailable();

        String out = captureStdout(() ->
                executor.execute(new ProcessBuilder("sh", "-c", "printf 'hello\\nworld\\n'"))
        );

        assertAll(
                () -> assertTrue(out.contains("hello"), "stdout must contain 'hello'"),
                () -> assertTrue(out.contains("world"), "stdout must contain 'world'")
        );
    }

    /**
     * Ensures stderr is not mixed into stdout (current implementation reads only getInputStream()).
     */
    @Test
    @DisplayName("Does not stream stderr to System.out")
    void doesNotStreamStderrToSystemOut() throws Exception {
        assumePosixShellAvailable();

        String out = captureStdout(() ->
                executor.execute(new ProcessBuilder("sh", "-c", "echo out; echo err 1>&2; exit 0"))
        );

        assertAll(
                () -> assertTrue(out.contains("out"), "stdout must contain 'out'"),
                () -> assertFalse(out.contains("err"), "stderr must not be present in captured stdout")
        );
    }

    /**
     * Ensures IOException is propagated when the executable cannot be started.
     */
    @Test
    @DisplayName("Propagates IOException when executable does not exist")
    void throwsIOExceptionWhenExecutableMissing() {
        ProcessBuilder pb = new ProcessBuilder("/this/definitely/does/not/exist");

        assertThrows(IOException.class, () -> executor.execute(pb));
    }

    private static void assumePosixShellAvailable() {
        String os = System.getProperty("os.name", "").toLowerCase();
        assumeTrue(
                os.contains("linux") || os.contains("unix") || os.contains("mac"),
                "These tests require a POSIX-like environment with 'sh'."
        );
    }

    private static String captureStdout(ThrowingCallable action) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream interceptor = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        try {
            System.setOut(interceptor);
            action.call();
            interceptor.flush();
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(original);
        }
    }

    @FunctionalInterface
    private interface ThrowingCallable {
        void call() throws Exception;
    }
}
