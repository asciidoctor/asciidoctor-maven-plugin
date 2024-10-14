package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.BlockImpl;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeProcessorProvider;

/**
 * Literal (aka. monospace) text processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class LiteralNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink                  Doxia {@link Sink}
     * @param nodeProcessorProvider
     */
    public LiteralNodeProcessor(Sink sink, NodeProcessorProvider nodeProcessorProvider) {
        super(sink, nodeProcessorProvider);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "literal".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final Sink sink = getSink();

        sink.division();
        sink.rawText("<pre>");
        // TODO see if this can be delegated now
        node.getBlocks().forEach(this::next);
        // contentBuilder.append(((BlockImpl) node).getSource());

        sink.rawText("/<pre>");
        sink.division_();
    }
}
