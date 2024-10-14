package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessorProvider;

/**
 * Recommended base case to build a {@link org.asciidoctor.maven.site.parser.NodeProcessor}.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class AbstractSinkNodeProcessor {

    private final Sink sink;

    private final NodeProcessorProvider nodeProcessorProvider;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public AbstractSinkNodeProcessor(Sink sink, NodeProcessorProvider nodeProcessorProvider) {
        this.sink = sink;
        this.nodeProcessorProvider = nodeProcessorProvider;
    }

    /**
     * Returns internal {@link Sink}.
     *
     * @return Doxia {@link Sink}
     */
    protected Sink getSink() {
        return sink;
    }

    // TODO rethink names
    protected void next(StructuralNode node) {
        nodeProcessorProvider.sink(node);
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
