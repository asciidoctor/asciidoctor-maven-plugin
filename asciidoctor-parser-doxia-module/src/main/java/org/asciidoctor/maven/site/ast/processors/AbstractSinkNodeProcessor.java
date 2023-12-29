package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ContentNode;

public class AbstractSinkNodeProcessor {

    private final Sink sink;

    public AbstractSinkNodeProcessor(Sink sink) {
        this.sink = sink;
    }

    protected Sink getSink() {
        return sink;
    }


    /**
     * Tests for the presence of an attribute in current and parent nodes.
     *
     * @param name attribute name
     * @param node node to check
     * @return true if attribute is found
     */
    protected boolean hasAttribute(String name, ContentNode node) {
        ContentNode current = node;
        while (current != null) {
            if (current.getAttribute(name) != null)
                return true;
            current = current.getParent();
        }
        return false;
    }
}
