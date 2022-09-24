package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;

import java.util.List;

public class UnorderedListNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private ListItemNodeProcessor itemNodeProcessor;

    public UnorderedListNodeProcessor(Sink sink) {
        super(sink);
    }

    public void setItemNodeProcessor(ListItemNodeProcessor itemNodeProcessor) {
        this.itemNodeProcessor = itemNodeProcessor;
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "ulist".equals(node.getNodeName());
    }

    @Override
    public boolean isTerminal(StructuralNode node) {
        return true;
    }

    // TODO title
    // TODO custom markers https://docs.asciidoctor.org/asciidoc/latest/lists/unordered/#markers
    @Override
    public void process(StructuralNode node) {
        final List<StructuralNode> items = node.getBlocks();
        final Sink sink = getSink();

        if (!items.isEmpty()) {
            sink.list();
            for (StructuralNode item : items) {
                if (itemNodeProcessor.applies(item)) {
                    itemNodeProcessor.process(item);
                }
            }
            sink.list_();
        }
    }
}