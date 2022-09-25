package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

public class PreambleNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    public PreambleNodeProcessor(Sink sink) {
        super(sink);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "preamble".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        getSink().rawText((String) node.getContent());
    }
}
