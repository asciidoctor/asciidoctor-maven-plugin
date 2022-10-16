package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

public class ParagraphNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

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
        String content = (String) node.getContent();
        getSink().rawText(content);
        getSink().paragraph_();
    }
}
