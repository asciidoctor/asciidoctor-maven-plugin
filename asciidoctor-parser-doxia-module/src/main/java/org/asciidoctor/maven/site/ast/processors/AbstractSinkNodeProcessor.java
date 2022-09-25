package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;

public class AbstractSinkNodeProcessor {

    private final Sink sink;

    public AbstractSinkNodeProcessor(Sink sink) {
        this.sink = sink;
    }

    protected Sink getSink() {
        return sink;
    }

}
