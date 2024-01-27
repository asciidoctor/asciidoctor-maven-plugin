package org.asciidoctor.maven.site.ast.processors;

import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

/**
 * Ordered list processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class OrderedListNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private ListItemNodeProcessor itemNodeProcessor;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public OrderedListNodeProcessor(Sink sink) {
        super(sink);
    }

    /**
     * Inject a {@link ListItemNodeProcessor}.
     *
     * @param nodeProcessor {@link ListItemNodeProcessor}
     */
    public void setItemNodeProcessor(ListItemNodeProcessor nodeProcessor) {
        this.itemNodeProcessor = nodeProcessor;
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
        final List<StructuralNode> items = node.getBlocks();
        final Sink sink = getSink();

        /**
         * doxia numberingStyle 0: 1. 2.
         *                      1: a. b.
         *                      2: A. B.
         *                      3: i. ii.
         *                      >: 1. 2.
         */
        if (!items.isEmpty()) {
            sink.numberedList(0);
            for (StructuralNode item : items) {
                if (itemNodeProcessor.applies(item)) {
                    itemNodeProcessor.process(item);
                }
            }
            sink.numberedList_();
        }
    }
}
