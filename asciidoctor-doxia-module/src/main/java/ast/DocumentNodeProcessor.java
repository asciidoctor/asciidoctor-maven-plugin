package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;

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
        getSink().rawText(HtmlHelper.h1(node.getTitle()));
    }
}
