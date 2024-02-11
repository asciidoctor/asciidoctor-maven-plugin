package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;

/**
 * Paragraph processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class ParagraphNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public ParagraphNodeProcessor(Sink sink) {
        super(sink);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "paragraph".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        getSink().paragraph();
        // content returns HTML processed including bold, italics, monospace, etc. attributes resolution
        // TODO run convert() instead of getContent?
        String content = (String) node.getContent();
        getSink().rawText(content);
        getSink().paragraph_();
    }
}
