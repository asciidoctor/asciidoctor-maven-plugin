package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;

public class ListingNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private final ListItemNodeProcessor itemNodeProcessor;

    public ListingNodeProcessor(Sink sink) {
        super(sink);
        this.itemNodeProcessor = new ListItemNodeProcessor(sink);
    }

    public ListingNodeProcessor(Sink sink, ListItemNodeProcessor itemNodeProcessor) {
        super(sink);
        this.itemNodeProcessor = itemNodeProcessor;
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "listing".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        // TODO
    }
}
