package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;

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
    public DocumentNodeProcessor(Sink sink, NodeSinker nodeSinker) {
        super(sink, nodeSinker);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "document".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final Sink sink = getSink();

        sink.body();
        // TODO review how this fits with Section titles also being 1
        sink.sectionTitle1();
        sink.rawText(node.getTitle());
        sink.sectionTitle1_();

        node.getBlocks()
            .forEach(this::sink);

        sink.body_();
    }
}
