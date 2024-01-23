package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ContentNode;

/**
 * Recommended base case to build a {@link org.asciidoctor.maven.site.ast.NodeProcessor}.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class AbstractSinkNodeProcessor {

    private final Sink sink;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public AbstractSinkNodeProcessor(Sink sink) {
        this.sink = sink;
    }

    /**
     * Returns internal {@link Sink}.
     *
     * @return Doxia {@link Sink}
     */
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
