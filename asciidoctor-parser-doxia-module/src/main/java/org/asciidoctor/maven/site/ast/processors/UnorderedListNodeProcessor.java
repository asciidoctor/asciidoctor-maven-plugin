package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

import java.util.List;

public class UnorderedListNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private ListItemNodeProcessor itemNodeProcessor;

    public UnorderedListNodeProcessor(Sink sink) {
        super(sink);
    }

    public void setItemNodeProcessor(ListItemNodeProcessor itemNodeProcessor) {
        this.itemNodeProcessor = itemNodeProcessor;
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "ulist".equals(node.getNodeName());
    }

    @Override
    public boolean isTerminal(StructuralNode node) {
        return true;
    }

    @Override
    public void process(StructuralNode node) {
        final List<StructuralNode> items = node.getBlocks();
        final Sink sink = getSink();

        if (!items.isEmpty()) {
            sink.list();
            for (StructuralNode item : items) {
                if (itemNodeProcessor.applies(item)) {
                    itemNodeProcessor.process(item);
                }
            }
            sink.list_();
        }
    }
}
