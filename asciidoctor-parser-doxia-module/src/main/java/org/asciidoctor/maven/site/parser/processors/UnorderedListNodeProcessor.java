package org.asciidoctor.maven.site.parser.processors;

import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeProcessorProvider;

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
     * @param sink                  Doxia {@link Sink}
     * @param nodeProcessorProvider
     */
    public UnorderedListNodeProcessor(Sink sink, NodeProcessorProvider nodeProcessorProvider) {
        super(sink, nodeProcessorProvider);
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
            node.getBlocks().forEach(this::next);
            sink.list_();
        }
    }
}
