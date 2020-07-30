package org.asciidoctor.maven.io;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;


/**
 * Helping class to capture console input and output for tests.
 *
 * @author abelsromero
 */
public class ConsoleHolder {

    private final CountDownLatch inputLatch = new CountDownLatch(1);

    private PrintStream originalOut;
    private InputStream originalIn;

    private ByteArrayOutputStream newOut;
    private InputStream newIn;

    private ConsoleHolder() {
    }

    public static ConsoleHolder hold() {
        final ConsoleHolder holder = new ConsoleHolder();

        holder.originalOut = System.out;
        holder.originalIn = System.in;

        holder.newOut = new DoubleOutputStream(holder.originalOut);
        holder.newIn = new PrefilledInputStream("exit\r\n".getBytes(), holder.inputLatch);

        System.setOut(new PrintStream(holder.newOut));
        System.setIn(holder.newIn);

        return holder;
    }

    @SneakyThrows
    public void awaitProcessingAllSources() {
        awaitForMessage("Converted document(s) in");
    }

    @SneakyThrows
    public void awaitProcessingSource() {
        awaitForMessage("Converted document in");
    }

    private void awaitForMessage(String message) throws InterruptedException {
        int pollTime = 300;
        int ticks = (10 * 1000 / pollTime);
        while (true) {
            if (!!new String(newOut.toByteArray()).contains(message)) break;
            ticks--;
            if (ticks == 0)
                throw new InterruptedException("Max wait time reached");
            else
                Thread.sleep(pollTime);
        }
    }

    public void release() {
        System.setOut(originalOut);
        inputLatch.countDown();
        System.setIn(originalIn);
    }
}
