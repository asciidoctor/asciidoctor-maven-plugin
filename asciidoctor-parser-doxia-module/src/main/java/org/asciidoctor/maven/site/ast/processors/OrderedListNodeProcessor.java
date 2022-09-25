package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

import java.util.List;

public class OrderedListNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private ListItemNodeProcessor itemNodeProcessor;

    public OrderedListNodeProcessor(Sink sink) {
        super(sink);
    }

    public void setItemNodeProcessor(ListItemNodeProcessor itemNodeProcessor) {
        this.itemNodeProcessor = itemNodeProcessor;
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "olist".equals(node.getNodeName());
    }

    @Override
    public boolean isTerminal(StructuralNode node) {
        return true;
    }

    // TODO: support asciidoctor levels in same order
    //   1 -> number
    //   2 -> lower case
    //   3 -> roman
    //   4 -> upper case
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
