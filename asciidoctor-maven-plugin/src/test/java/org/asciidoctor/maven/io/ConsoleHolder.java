package org.asciidoctor.maven.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import lombok.SneakyThrows;


/**
 * Helping class to capture console input and output for tests.
 *
 * @author abelsromero
 */
public class ConsoleHolder {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private InputStream originalIn;

    private ByteArrayOutputStream newOut;
    private ByteArrayOutputStream newErr;
    private InputStream newIn;

    private ConsoleHolder() {
    }

    public static ConsoleHolder start() {
        final ConsoleHolder holder = new ConsoleHolder();

        holder.originalOut = System.out;
        holder.originalErr = System.err;
        holder.originalIn = System.in;

        holder.newOut = new DoubleOutputStream(holder.originalOut);
        holder.newErr = new DoubleOutputStream(holder.originalErr);
        holder.newIn = new StringsCollectionsInputStream();

        System.setOut(new PrintStream(holder.newOut));
        System.setErr(new PrintStream(holder.newErr));
        System.setIn(holder.newIn);

        return holder;
    }

    public void awaitProcessingAllSources() {
        awaitForMessage("Converted document(s) in");
    }

    public void awaitProcessingSource() {
        awaitForMessage("Converted document in");
    }

    public void awaitProcessingResource() {
        awaitForMessage("Copied resource in");
    }

    int cursor = 0;

    @SneakyThrows
    public void awaitForMessage(String message) {
        int pollTime = 300;
        int ticks = (10 * 1500 / pollTime);
        while (true) {
            int pos = new String(newOut.toByteArray()).indexOf(message, cursor);
            if (pos > 0) {
                cursor = pos + message.length();
                break;
            }
            ticks--;
            if (ticks == 0)
                throw new InterruptedException("Max wait time reached");
            else
                Thread.sleep(pollTime);
        }
    }

    @SneakyThrows
    public void input(String command) {
        ((StringsCollectionsInputStream) newIn).publishLine(command);
    }

    public void release() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    public String getOutput() {
        return new String(newOut.toByteArray());
    }

    public String getError() {
        return new String(newErr.toByteArray());
    }
}
