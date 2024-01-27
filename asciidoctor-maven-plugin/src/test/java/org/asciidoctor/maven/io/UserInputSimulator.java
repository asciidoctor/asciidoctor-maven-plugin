package org.asciidoctor.maven.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

import lombok.SneakyThrows;

public class UserInputSimulator {

    private final CountDownLatch inputLatch;
    private final PrintStream originalOut;
    private final ByteArrayOutputStream newOut;

    public UserInputSimulator() {
        inputLatch = new CountDownLatch(1);
        originalOut = System.out;
        newOut = new DoubleOutputStream(originalOut);
    }

    /**
     * Simulates commands inputted by an user.
     */
    public void type(String input) {
        System.setOut(new PrintStream(newOut));
        System.setIn(new PrefilledInputStream(input.getBytes(), inputLatch));
    }

    public void stop() {
        System.setOut(originalOut);
        inputLatch.countDown();
    }

    @SneakyThrows
    public void waitForCompletion() {
        while (!new String(newOut.toByteArray()).contains("Type ")) {
            Thread.sleep(200);
        }
    }
}
