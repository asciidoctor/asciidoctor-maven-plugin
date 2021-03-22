package org.asciidoctor.maven.io;

import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CountDownLatch;

public class PrefilledInputStream extends ByteArrayInputStream {

    final CountDownLatch latch;

    public PrefilledInputStream(final byte[] buf, final CountDownLatch latch) {
        super(buf);
        this.latch = latch;
    }

    @SneakyThrows
    @Override
    public synchronized int read(final byte[] b, final int off, final int len) {
        latch.await();
        return super.read(b, off, len);
    }
}
