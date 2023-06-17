package org.asciidoctor.maven.io;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reads from a collection of strings to simulate command line inputs.
 * {@link InputStream#read(byte[], int, int)} will wait indefinitely
 * until a new line is published with {@link #publishLine(String)}.
 */
class StringsCollectionsInputStream extends InputStream {

    private static final String LINEBREAK = "\r\n";

    private final AtomicInteger index = new AtomicInteger(0);
    private final List<String> lines = new ArrayList<>();

    private volatile Semaphore mutex = new Semaphore(1);

    @SneakyThrows
    public StringsCollectionsInputStream() {
        mutex.acquire();
    }

    @SneakyThrows
    @Override
    public int read() {
        mutex.acquire();
        int indexValue = index.get();
        return indexValue >= lines.size() ? -1 : lines.get(indexValue).charAt(0);

    }

    @SneakyThrows
    @Override
    public int read(byte[] b, final int off, final int len) {
        mutex.acquire();

        if (lines.isEmpty()) {
            return copyBytesToBuffer(LINEBREAK, b, off);
        } else {
            return copyBytesToBuffer(lines.get(index.getAndIncrement()), b, off);
        }
    }

    private int copyBytesToBuffer(String line, byte[] buffer, int off) {
        byte[] bytes = (line + LINEBREAK).getBytes();
        for (int i = 0; i < bytes.length; i++) {
            buffer[off + i] = bytes[i];
        }
        return bytes.length;
    }

    public void publishLine(String line) {
        lines.add(line);
        mutex.release();
    }
}
