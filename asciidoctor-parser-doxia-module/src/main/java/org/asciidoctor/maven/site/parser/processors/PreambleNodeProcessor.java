package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeProcessorProvider;

/**
 * Document preamble processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class PreambleNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink                  Doxia {@link Sink}
     * @param nodeProcessorProvider
     */
    public PreambleNodeProcessor(Sink sink, NodeProcessorProvider nodeProcessorProvider) {
        super(sink, nodeProcessorProvider);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "preamble".equals(node.getNodeName());
    }

    /**
     * Do nothing, preamble only aggregates other blocks.
     **/
    @Override
    public void process(StructuralNode node) {
        node.getBlocks().forEach(this::next);
    }
}
