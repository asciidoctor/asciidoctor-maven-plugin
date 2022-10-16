package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

public class DocumentNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    public DocumentNodeProcessor(Sink sink) {
        super(sink);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "document".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        // NOTE: H1 generation fixed in doxia 2.0.0-MX
        getSink().rawText(String.format("<h1>%s</h1>", node.getTitle()));
    }
}
