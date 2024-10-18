package org.asciidoctor.maven.site.parser.processors;

import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;

/**
 * Ordered list processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class OrderedListNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink       Doxia {@link Sink}
     * @param nodeSinker
     */
    public OrderedListNodeProcessor(Sink sink, NodeSinker nodeSinker) {
        super(sink, nodeSinker);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "olist".equals(node.getNodeName());
    }

    @Override
    public boolean isTerminal(StructuralNode node) {
        return true;
    }

    @Override
    public void process(StructuralNode node) {
        final List<StructuralNode> subNodes = node.getBlocks();
        final Sink sink = getSink();

        /*
         * doxia numberingStyle 0: 1. 2.
         *                      1: a. b.
         *                      2: A. B.
         *                      3: i. ii.
         *                      >: 1. 2.
         */
        if (!subNodes.isEmpty()) {
            sink.numberedList(0);
            subNodes.forEach(this::sink);
            sink.numberedList_();
        }
    }
}
