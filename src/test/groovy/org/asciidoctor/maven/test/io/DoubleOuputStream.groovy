package org.asciidoctor.maven.test.io

class DoubleOuputStream extends ByteArrayOutputStream {
    final OutputStream other

    DoubleOuputStream(final OutputStream os) {
        other = os
    }

    @Override
    public synchronized void write(final byte[] b, final int off, final int len) {
        other.write(b, off, len)
        super.write(b, off, len)
    }
}
