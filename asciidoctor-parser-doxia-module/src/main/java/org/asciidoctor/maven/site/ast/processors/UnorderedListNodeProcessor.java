package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

import java.util.List;

/**
 * Unordered list processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class UnorderedListNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private ListItemNodeProcessor itemNodeProcessor;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public UnorderedListNodeProcessor(Sink sink) {
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
