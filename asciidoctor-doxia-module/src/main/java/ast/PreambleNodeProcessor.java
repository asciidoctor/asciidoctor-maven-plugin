package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;

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
        // TODO
        final Sink sink = getSink();

        sink.paragraph();
        sink.text("some text");
        sink.paragraph_();
    }
}
