package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeSinker;

/**
 * Recommended base case to build a {@link org.asciidoctor.maven.site.parser.NodeProcessor}.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class AbstractSinkNodeProcessor {

    private final Sink sink;
    private final NodeSinker nodeSinker;

    /**
     * Constructor.
     *
     * @param sink       Doxia {@link Sink}
     * @param nodeSinker
     */
    public AbstractSinkNodeProcessor(Sink sink, NodeSinker nodeSinker) {
        this.sink = sink;
        this.nodeSinker = nodeSinker;
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
     * Delegates the processing of the new node to the appropriate processor.
     * Similar to {@link org.asciidoctor.maven.site.parser.NodeProcessor#process(StructuralNode)}
     * but this selects the processor from the ones available.
     *
     * @param node Node to process
     */
    protected void sink(StructuralNode node) {
        nodeSinker.sink(node);
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
