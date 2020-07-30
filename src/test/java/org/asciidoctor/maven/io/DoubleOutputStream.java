package org.asciidoctor.maven.io;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

class DoubleOutputStream extends ByteArrayOutputStream {

    final OutputStream other;

    public DoubleOutputStream(final OutputStream os) {
        other = os;
    }

    @SneakyThrows
    @Override
    public synchronized void write(final byte[] b, final int off, final int len) {
        other.write(b, off, len);
        super.write(b, off, len);
    }
}
