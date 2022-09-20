package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;

import java.util.Arrays;
import java.util.List;

public class ListItemNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private List<NodeProcessor> nodeProcessors;

    // TODO refactor construction, maybe add nodeProcessors to super Abstract o create another Abstract (NestedNodeProcessor?)
    // we are creating an extra *ListNodeProcessor instance
    public ListItemNodeProcessor(Sink sink) {
        super(sink);

    }

    public void setNodeProcessors(List<NodeProcessor> nodeProcessors) {
        this.nodeProcessors = nodeProcessors;
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "list_item".equals(node.getNodeName());
    }

    // TODO title
    // TODO custom markers https://docs.asciidoctor.org/asciidoc/latest/lists/unordered/#markers
    @Override
    public void process(StructuralNode node) {
        final ListItem item = (ListItem) node;
        final Sink sink = getSink();
        if (isUnorderedListItem(item))
            sink.listItem();
        else
            sink.numberedListItem();

        sink.text(item.getText());

        for (StructuralNode subNode : node.getBlocks()) {
            for (NodeProcessor np : nodeProcessors) {
                if (np.applies(subNode)) {
                    np.process(subNode);
                }
            }
        }

        if (isUnorderedListItem(item))
            sink.listItem_();
        else
            sink.numberedListItem_();
    }

    private static boolean isUnorderedListItem(ListItem item) {
        String marker = item.getMarker();
        return marker.startsWith("*") || marker.startsWith("-");
    }
}
