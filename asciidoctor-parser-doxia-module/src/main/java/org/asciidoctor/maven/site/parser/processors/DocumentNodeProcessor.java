package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeProcessorProvider;

/**
 * Root document processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class DocumentNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public DocumentNodeProcessor(Sink sink, NodeProcessorProvider nodeProcessorProvider) {
        super(sink, nodeProcessorProvider);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "document".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        getSink().body();
        // NOTE: H1 generation fixed in doxia 2.0.0-MX
        getSink().rawText(String.format("<h1>%s</h1>", node.getTitle()));

        node.getBlocks()
            .forEach(super::next);

        getSink().body_();
    }
}
